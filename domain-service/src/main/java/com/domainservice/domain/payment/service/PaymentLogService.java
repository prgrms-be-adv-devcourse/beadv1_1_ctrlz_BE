package com.domainservice.domain.payment.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.domainservice.domain.payment.model.entity.PaymentLogEntity;
import com.domainservice.domain.payment.repository.PaymentLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentLogService {

    private final PaymentLogRepository paymentLogRepository;

    // ✔️ 로그는 메인 트랜잭션과 독립적으로 처리해야 함
    // PG사의 통신 오류 등으로 예외가 발생하여 롤백되어도 로그 기록은 별도의 트랜잭션으로 커밋되어 DB에 실패 기록이 남도록
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(
        String orderId,
        String usersId,
        String paymentKey,
        String status,
        String requestJson,
        String responseJson,
        String failReason) {

        PaymentLogEntity log = PaymentLogEntity.builder()
            .orderId(orderId)
            .usersId(usersId)
            .paymentKey(paymentKey)
            .status(status)
            .requestBody(requestJson)
            .responseBody(responseJson)
            .failReason(failReason)
            .loggedAt(LocalDateTime.now())
            .build();

        paymentLogRepository.save(log);
    }
}
