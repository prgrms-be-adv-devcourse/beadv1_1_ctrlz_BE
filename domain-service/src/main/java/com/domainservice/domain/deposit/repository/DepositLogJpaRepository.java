package com.domainservice.domain.deposit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.domainservice.domain.deposit.model.entity.DepositLog;

public interface DepositLogJpaRepository extends JpaRepository<DepositLog, String> {
}
