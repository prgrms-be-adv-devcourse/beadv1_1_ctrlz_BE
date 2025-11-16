package com.domainservice.domain.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.domainservice.domain.payment.model.entity.PaymentEntity;

public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {
    Optional<PaymentEntity> findByPaymentKey(String paymentKey);

    Optional<PaymentEntity> findByOrder_Id(String orderId);
}
