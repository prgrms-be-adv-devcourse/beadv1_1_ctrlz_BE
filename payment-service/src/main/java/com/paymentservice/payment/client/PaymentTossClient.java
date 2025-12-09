package com.paymentservice.payment.client;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
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
    public TossApprovalResponse approve(PaymentConfirmRequest request) {

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


    // 환불
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


    private String authHeader() {
        String key = (secretApiKey != null) ? secretApiKey : "test_secret_key";
        return "Basic " + Base64.getEncoder()
            .encodeToString((key + ":").getBytes(StandardCharsets.UTF_8));

    }
}
