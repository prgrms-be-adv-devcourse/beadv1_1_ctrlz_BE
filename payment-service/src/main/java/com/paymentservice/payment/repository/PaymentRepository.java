package com.paymentservice.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paymentservice.payment.model.entity.PaymentEntity;

public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {
    Optional<PaymentEntity> findByPaymentKey(String paymentKey);

    Optional<PaymentEntity> findByOrderId(String orderId);

    java.util.List<PaymentEntity> findByApprovedAtBetweenAndStatus(
            java.time.OffsetDateTime start,
            java.time.OffsetDateTime end,
            com.paymentservice.payment.model.enums.PaymentStatus status);
}
