package com.domainservice.domain.deposit.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.domainservice.domain.deposit.model.entity.Deposit;

public interface DepositJpaRepository extends JpaRepository<Deposit, String> {
	Optional<Deposit> findByUserId(String userId);
}
