package com.paymentservice.deposit.client;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.paymentservice.common.configuration.feign.client.PaymentFeignClient;
import com.paymentservice.deposit.model.dto.DepositConfirmRequest;
import com.paymentservice.deposit.model.dto.TossChargeResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositTossClient {

    private final PaymentFeignClient paymentFeignClient;

    @Value("${custom.payment.toss.test_secret_api_key}")
    private String secretApiKey;

    // 충전 성공
    public TossChargeResponse approve(String userId, DepositConfirmRequest request) {

        // Toss로 보내야하는 필수 필드
        Map<String, Object> requestBody = Map.of(
            "paymentKey", request.paymentKey(),
            "orderId", request.orderId(),
            "amount", request.amount()
        );

        Map<String, Object> responseMap = paymentFeignClient.requestPayment(
            requestBody, authHeader()
        );

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

        BigDecimal approvedAmount = null;
        Object amountObj = responseMap.get("amount");
        if (amountObj != null) {
            approvedAmount = new BigDecimal(amountObj.toString());
        } else {
            approvedAmount = request.amount();
        }

        String paymentKey = responseMap.get("paymentKey") != null ? responseMap.get("paymentKey").toString() : request.paymentKey();
        String currency = responseMap.get("currency") != null ? responseMap.get("currency").toString() : "KRW";

        return new TossChargeResponse(
            userId,
            approvedAmount,
            paymentKey,
            currency,
            approvedAt
        );

    }

    private String authHeader() {
        String key = (secretApiKey != null) ? secretApiKey : "test_secret_key";
        return "Basic " + Base64.getEncoder()
            .encodeToString((key + ":").getBytes(StandardCharsets.UTF_8));

    }
}
