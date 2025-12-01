package com.paymentservice.payment.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentservice.payment.model.entity.PaymentLogEntity;
import com.paymentservice.payment.repository.PaymentLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentLogService {

    private final PaymentLogRepository paymentLogRepository;
    private final ObjectMapper objectMapper;

    public void logRequest(String userId, String orderId, String paymentKey, Object payload) {
        saveLog(
            orderId,
            userId,
            paymentKey,
            "REQUEST",
            toJson(payload),
            null,
            null
        );
    }

    public void logSuccess(String orderId, String userId, String paymentKey, Object payload, Object response) {
        saveLog(
            orderId,
            userId,
            paymentKey,
            "SUCCESS",
            toJson(payload),
            toJson(response),
            null
        );
    }

    public void logFail(String orderId, String userId, String paymentKey, String failReason, Object payload) {
        saveLog(
            orderId,
            userId,
            paymentKey,
            "FAIL",
            toJson(payload),
            null,
            failReason
        );
    }


    // ✔️ 로그는 메인 트랜잭션과 독립적으로 처리해야 함
    // PG사의 통신 오류 등으로 예외가 발생하여 롤백되어도 로그 기록은 별도의 트랜잭션으로 커밋되어 DB에 실패 기록이 남도록
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void saveLog(
        String orderId,
        String userId,
        String paymentKey,
        String status,
        String requestJson,
        String responseJson,
        String failReason
    ) {
        PaymentLogEntity log = PaymentLogEntity.builder()
            .orderId(orderId)
            .usersId(userId)
            .paymentKey(paymentKey)
            .status(status)
            .requestBody(requestJson)
            .responseBody(responseJson)
            .failReason(failReason)
            .loggedAt(LocalDateTime.now())
            .build();

        paymentLogRepository.save(log);
    }

    private String toJson(Object payload) {
        try {
            return payload == null ? null : objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            return "{\"error\": \"failed to serialize payload\"}";
        }
    }
}