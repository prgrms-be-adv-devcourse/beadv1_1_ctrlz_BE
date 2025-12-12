package com.paymentservice.payment.repository;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paymentservice.payment.model.entity.PaymentEntity;
import com.paymentservice.payment.model.enums.PaymentStatus;

public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {
    Optional<PaymentEntity> findByPaymentKey(String paymentKey);

    Optional<PaymentEntity> findByOrderId(String orderId);

    java.util.List<PaymentEntity> findByApprovedAtBetweenAndStatus(
            OffsetDateTime start,
            OffsetDateTime end,
            PaymentStatus status);
}
