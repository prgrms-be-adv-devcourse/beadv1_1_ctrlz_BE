package com.paymentservice.payment.client;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentservice.common.configuration.feign.client.PaymentFeignClient;
import com.paymentservice.payment.exception.PaymentGatewayFailedException;
import com.paymentservice.payment.model.dto.PaymentConfirmRequest;
import com.paymentservice.payment.model.dto.RefundResponse;
import com.paymentservice.payment.model.dto.TossApprovalResponse;
import com.paymentservice.payment.model.dto.TossApproveRequest;
import com.paymentservice.payment.model.dto.TossCancelRequest;
import com.paymentservice.payment.model.entity.PaymentEntity;
import com.paymentservice.payment.model.enums.PaymentStatus;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentTossClient {

    private final PaymentFeignClient paymentFeignClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${custom.payment.toss.test_secret_api_key}")
    private String secretApiKey;

    // 결제 승인
    @Retryable(
        value = {FeignException.class, RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000) // 1초 쉬고 다시 재시도
    )
    public TossApprovalResponse approve(PaymentConfirmRequest request) {

        TossApproveRequest requestBody = new TossApproveRequest(request.paymentKey(), request.orderId(),
            request.totalAmount());

        Map<String, Object> responseMap = paymentFeignClient.requestPayment(
                requestBody, authHeader());

        String responseStatus = (String) responseMap.get("status");
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
                (String) responseMap.get("currency"),
                null,
                paymentStatus,
                (String) responseMap.get("paymentKey"),
                approvedAt);
    }

    // 환불
    @Retryable(
        value = {FeignException.class, RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    public RefundResponse refund(PaymentEntity payment) {

        TossCancelRequest cancelBody = new TossCancelRequest(payment.getTossChargedAmount(), "사용자 요청 환불");
        RefundResponse response = paymentFeignClient.refundPayment(
                payment.getPaymentKey(),
                cancelBody,
                authHeader());

        // Toss 환불 실패 시 바로 Exception
        if (!Objects.equals("CANCELED", response.status())) {
            throw new PaymentGatewayFailedException();
        }

        return response;
    }

    // Entity없이 키값 만으로 취소하는 기능
    public void cancelPayment(String paymentKey, String reason) {
        try {
            TossCancelRequest cancelRequest = new TossCancelRequest(null, reason); // 금액이 null이면 전액 취소
            paymentFeignClient.refundPayment(paymentKey, cancelRequest, authHeader());
            log.info("결제 취소(보상 트랜잭션) 성공: paymentKey={}, reason={}", paymentKey, reason);
        } catch (Exception e) {
            log.error("결제 취소(보상 트랜잭션) 실패 - 수동 환불 필요: paymentKey={}, reason={}", paymentKey, reason, e);
            // 취소 실패는 Exception을 던지지 않고 로그만 남겨서 원본 에러(Exception e)가 묻히지 않게 함
        }
    }

    private String authHeader() {
        String key = (secretApiKey != null) ? secretApiKey : "test_secret_key";
        return "Basic " + Base64.getEncoder()
                .encodeToString((key + ":").getBytes(StandardCharsets.UTF_8));

    }
}
