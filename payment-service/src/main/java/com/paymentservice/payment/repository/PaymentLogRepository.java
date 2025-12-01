package com.paymentservice.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paymentservice.payment.model.entity.PaymentLogEntity;

public interface PaymentLogRepository extends JpaRepository<PaymentLogEntity, String> {
}
