package com.domainservice.domain.deposit.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.domainservice.domain.deposit.model.entity.Deposit;

import jakarta.persistence.LockModeType;

public interface DepositJpaRepository extends JpaRepository<Deposit, String> {
	Optional<Deposit> findByUserId(String userId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select d from Deposit d where d.userId = :userId")
	Optional<Deposit> findByUserIdForUpdate(@Param("userId") String userId);
}
