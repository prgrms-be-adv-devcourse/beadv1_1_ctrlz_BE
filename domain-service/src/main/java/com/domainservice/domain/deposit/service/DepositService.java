package com.domainservice.domain.deposit.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.exception.CustomException;
import com.common.exception.vo.DepositExceptionCode;
import com.domainservice.domain.deposit.model.dto.DepositResponse;
import com.domainservice.domain.deposit.model.entity.Deposit;
import com.domainservice.domain.deposit.model.entity.DepositLog;
import com.domainservice.domain.deposit.model.entity.TransactionType;
import com.domainservice.domain.deposit.repository.DepositJpaRepository;
import com.domainservice.domain.deposit.repository.DepositLogJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DepositService {
	private final DepositJpaRepository depositJpaRepository;
	private final DepositLogJpaRepository depositLogJpaRepository;

	/**
	 * 사용자 ID로 예치금 정보 조회
	 * 사용자의 예치금 정보가 없으면 새로 생성
	 */
	@Transactional(readOnly = true)
	public Deposit getDepositByUserId(String userId) {
		return depositJpaRepository.findByUserId(userId)
			.orElseGet(
				() -> depositJpaRepository.save(Deposit.builder().userId(userId).balance(BigDecimal.ZERO).build()));
	}

	/**
	 * 사용자의 예치금 충전
	 */
	public DepositResponse chargeDeposit(String userId, BigDecimal amount) {
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new CustomException(DepositExceptionCode.INVALID_AMOUNT.getMessage());
		}

		Deposit deposit = getDepositByUserId(userId);
		BigDecimal beforeBalance = deposit.getBalance();
		deposit.increaseBalance(amount);
		BigDecimal afterBalance = deposit.getBalance();

		Deposit savedDeposit = depositJpaRepository.save(deposit);

		DepositLog depositLog = DepositLog.create(
			userId,
			savedDeposit,
			TransactionType.CHARGE,
			amount,
			beforeBalance,
			afterBalance
		);
		depositLogJpaRepository.save(depositLog);

		return new DepositResponse(savedDeposit.getId(), savedDeposit.getBalance(), "충전이 완료되었습니다.");
	}

	/**
	 * 사용자의 예치금 사용
	 */
	public DepositResponse useDeposit(String userId, BigDecimal amount) {
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new CustomException(DepositExceptionCode.INVALID_AMOUNT.getMessage());
		}

		Deposit deposit = getDepositByUserId(userId);
		BigDecimal beforeBalance = deposit.getBalance();

		if (deposit.getBalance().compareTo(amount) < 0) {
			throw new CustomException(DepositExceptionCode.INSUFFICIENT_BALANCE.getMessage());
		}

		deposit.decreaseBalance(amount);
		BigDecimal afterBalance = deposit.getBalance();

		Deposit savedDeposit = depositJpaRepository.save(deposit);

		DepositLog depositLog = DepositLog.create(
			userId,
			savedDeposit,
			TransactionType.PURCHASE,
			amount,
			beforeBalance,
			afterBalance
		);
		depositLogJpaRepository.save(depositLog);

		return new DepositResponse(savedDeposit.getId(), savedDeposit.getBalance(), "예치금 사용이 완료되었습니다.");
	}

	/**
	 * 사용자의 예치금 잔액 조회
	 */
	@Transactional(readOnly = true)
	public DepositResponse getDepositBalance(String userId) {
		Deposit deposit = getDepositByUserId(userId);
		return new DepositResponse(deposit.getId(), deposit.getBalance(), "잔액 조회가 완료되었습니다.");
	}

	/**
	 * 특정 유저의 예치금 확인 (외부 서비스 연동용)
	 */
	@Transactional(readOnly = true)
	public boolean hasEnoughDeposit(String userId, BigDecimal amount) {
		Deposit deposit = getDepositByUserId(userId);
		return deposit.getBalance().compareTo(amount) >= 0;
	}

}
