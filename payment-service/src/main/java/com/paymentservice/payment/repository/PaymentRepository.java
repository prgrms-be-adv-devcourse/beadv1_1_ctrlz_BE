package com.paymentservice.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paymentservice.payment.model.entity.PaymentEntity;

public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {
    Optional<PaymentEntity> findByOrderId(String orderId);
    boolean existsByOrderId(String orderId);
}
