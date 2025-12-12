package com.paymentservice.payment.client;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.paymentservice.common.configuration.feign.client.PaymentFeignClient;
import com.paymentservice.payment.exception.PaymentGatewayFailedException;
import com.paymentservice.payment.model.dto.PaymentConfirmRequest;
import com.paymentservice.payment.model.dto.RefundResponse;
import com.paymentservice.payment.model.dto.TossApprovalResponse;
import com.paymentservice.payment.model.dto.TossApproveRequest;
import com.paymentservice.payment.model.dto.TossCancelRequest;
import com.paymentservice.payment.model.entity.PaymentEntity;
import com.paymentservice.payment.model.enums.PaymentStatus;
import com.paymentservice.payment.repository.PaymentRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentTossClient {

    private final PaymentFeignClient paymentFeignClient;
    private final PaymentRepository paymentRepository;

    @Value("${custom.payment.toss.test_secret_api_key}")
    private String secretApiKey;

    // 결제 승인
    @Retryable(
        value = {FeignException.class, RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000) // 1초 쉬고 다시 재시도
    )
    public TossApprovalResponse approve(PaymentConfirmRequest request) {

        // 멱등성 보장(paymentKey + orderId 조합) DB 재요청 막기 -> 불필요한 외부 API막기
        if (paymentRepository.existsByOrderId(request.orderId())) {
            log.info("이미 처리된 결제입니다. DB 정보를 반환합니다. orderId={}", request.orderId());
            PaymentEntity existingPayment = paymentRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("결제 정보가 존재하지 않습니다."));


            // Entity -> TossApprovalResponse 변환
            return new TossApprovalResponse(
                existingPayment.getOrderId(),
                existingPayment.getAmount(),
                existingPayment.getDepositUsedAmount(),
                existingPayment.getTossChargedAmount(),
                existingPayment.getCurrency(),
                existingPayment.getPayType(),
                existingPayment.getStatus(),
                existingPayment.getPaymentKey(),
                existingPayment.getApprovedAt()
            );
        }

        // Toss로 보내야하는 필수 필드
        TossApproveRequest requestBody = TossApproveRequest.from(request);

        Map<String, Object> responseMap = paymentFeignClient.requestPayment(
            requestBody, authHeader()
        );

        String responseStatus = (String)responseMap.get("status");
        PaymentStatus paymentStatus;
        // Toss 응답 상태가 DONE이 아닌 경우는 실패
        if (Objects.equals("DONE", responseStatus)) {
            paymentStatus = PaymentStatus.SUCCESS;
        } else {
            throw new PaymentGatewayFailedException();
        }

        OffsetDateTime approvedAt = null;
        Object approvedAtObj = responseMap.get("approvedAt");
        if (approvedAtObj != null) {
            try {
                approvedAt = OffsetDateTime.parse(approvedAtObj.toString());
            } catch (DateTimeParseException e) {
                log.warn("approvedAt 파싱 실패, 현재 시간으로 대체", e);
                approvedAt = OffsetDateTime.now();
            }
        }
        return new TossApprovalResponse(
            request.orderId(),
            request.amount(),
            request.usedDepositAmount(),
            request.totalAmount(),
            (String)responseMap.get("currency"),
            null,
            paymentStatus,
            (String)responseMap.get("paymentKey"),
            approvedAt
        );
    }

    @Recover
    public TossApprovalResponse recoverApprove(Exception e, PaymentConfirmRequest request) {
        log.error("Toss 승인 최종 실패 orderId={}", request.orderId());
        throw new PaymentGatewayFailedException();
    }

    // 환불
    @Retryable(
        value = {FeignException.class, RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    public RefundResponse refund(PaymentEntity payment) {

        // Toss로 보내야하는 필수 필드
        TossCancelRequest cancelBody = TossCancelRequest.from(payment);

        RefundResponse response = paymentFeignClient.refundPayment(
            payment.getPaymentKey(),
            cancelBody,
            authHeader()
        );

        // Toss 환불 실패 시 바로 Exception
        if (!Objects.equals("CANCELED", response.status())) {
            throw new PaymentGatewayFailedException();
        }

        return response;
    }

    @Recover
    public TossApprovalResponse recoverRefund(Exception e, PaymentConfirmRequest request) {
        log.error("Toss 환불 최종 실패 orderId={}", request.orderId());
        throw new PaymentGatewayFailedException();
    }

    private String authHeader() {
        String key = (secretApiKey != null) ? secretApiKey : "test_secret_key";
        return "Basic " + Base64.getEncoder()
            .encodeToString((key + ":").getBytes(StandardCharsets.UTF_8));

    }
}
