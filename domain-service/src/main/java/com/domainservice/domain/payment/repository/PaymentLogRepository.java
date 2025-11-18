package com.domainservice.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.domainservice.domain.payment.model.entity.PaymentLogEntity;

public interface PaymentLogRepository extends JpaRepository<PaymentLogEntity, String> {
}
