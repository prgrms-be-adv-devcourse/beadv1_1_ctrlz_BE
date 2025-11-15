package com.domainservice.domain.deposit.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.domainservice.domain.deposit.model.entity.DepositLog;
import com.domainservice.domain.deposit.model.entity.TransactionType;

public interface DepositLogJpaRepository extends JpaRepository<DepositLog, String> {
	boolean existsByTransactionTypeAndReferenceId(TransactionType transactionType, String referenceId);

	Optional<DepositLog> findByUserId(String userId);
}
