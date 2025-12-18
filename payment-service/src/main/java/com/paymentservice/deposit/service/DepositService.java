package com.paymentservice.deposit.service;

import static org.springframework.transaction.annotation.Propagation.*;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.exception.CustomException;
import com.common.exception.vo.DepositExceptionCode;
import com.paymentservice.deposit.client.DepositTossClient;
import com.paymentservice.deposit.model.dto.DepositConfirmRequest;
import com.paymentservice.deposit.model.dto.DepositConfirmResponse;
import com.paymentservice.deposit.model.dto.DepositResponse;
import com.paymentservice.deposit.model.dto.TossChargeResponse;
import com.paymentservice.deposit.model.entity.Deposit;
import com.paymentservice.deposit.model.entity.DepositLog;
import com.paymentservice.deposit.model.entity.TransactionType;
import com.paymentservice.deposit.repository.DepositJpaRepository;
import com.paymentservice.deposit.repository.DepositLogJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DepositService {
    private final DepositJpaRepository depositJpaRepository;
    private final DepositLogJpaRepository depositLogJpaRepository;
	private final DepositTossClient depositTossClient;

	/**
	 * 사용자 ID로 예치금 정보 조회
	 * 사용자의 예치금 정보가 없으면 새로 생성
	 */
	public Deposit getDepositByUserId(String userId) {
		return depositJpaRepository.findByUserId(userId)
			.orElseGet(
				() -> depositJpaRepository.save(Deposit.builder().userId(userId).balance(BigDecimal.ZERO).paymentKey("default-payment-key").build()));
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
	public Deposit getDepositBalance(String userId) {
		return getDepositByUserId(userId);
	}

    /**
     * 특정 유저의 예치금 확인 (외부 서비스 연동용)
     */
    @Transactional(readOnly = true)
    public boolean hasEnoughDeposit(String userId, BigDecimal amount) {
        Deposit deposit = getDepositByUserId(userId);
        return deposit.getBalance().compareTo(amount) >= 0;
    }

    /**
     * 예치금 환불
     */
    public DepositResponse refundUsedDeposit(String userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
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
            TransactionType.REFUND,
            amount,
            beforeBalance,
            afterBalance
        );
        depositLogJpaRepository.save(depositLog);

        return new DepositResponse(savedDeposit.getId(), savedDeposit.getBalance(), "환불이 완료되었습니다.");
    }

    /**
     * 결제 실패 시 사용한 예치금을 환불
     */
    public DepositResponse refundDeposit(String userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException(DepositExceptionCode.INVALID_AMOUNT.getMessage());
        }

        DepositResponse response = refundUsedDeposit(userId, amount);

        return new DepositResponse(response.depositId(), response.balance(), "예치금이 환불되었습니다.");
    }


	public Deposit createDeposit(String userId) {

		if(depositJpaRepository.findByUserId(userId).isPresent()){
			throw new CustomException(DepositExceptionCode.DEPOSIT_ALREADY_EXISTS.getMessage());
		}

		Deposit deposit = Deposit.of(userId,BigDecimal.ZERO);
		return depositJpaRepository.save(deposit);
	}

	/**
	 * Toss 결제 확정 처리
	 * - Toss 승인을 받고 (DepositTossClient.approve)
	 * - 승인된 금액과 요청 금액을 검증 (불일치 시 예외)
	 * - deposit balance 증가, paymentKey 저장, 로그 생성
	 */
	// PG 승인 요청 → 트랜잭션 없음
	@Transactional(propagation = NOT_SUPPORTED)
	public TossChargeResponse tossApprove(DepositConfirmRequest request, String userId) {
		return depositTossClient.approve(userId, request);
	}

	public DepositConfirmResponse tossPayment(TossChargeResponse approve, DepositConfirmRequest request, String userId) {
		try {

			// 승인된 금액과 요청 금액 검증 (안정성 체크)
			if (approve.balance() == null || request.amount() == null ||
				approve.balance().compareTo(request.amount()) != 0) {
				log.warn("토스승인 금액과 충전 금액이 다릅니다.. requested={}, approved={}", request.amount(), approve.balance());
				throw new CustomException(DepositExceptionCode.INVALID_AMOUNT.getMessage());
			}

			// 예치금 계정 조회 및 갱신
			Deposit deposit = getDepositByUserId(userId);
			BigDecimal beforeBalance = deposit.getBalance();

			deposit.increaseBalance(request.amount());
			deposit.setPaymentKey(approve.paymentKey());

			Deposit savedDeposit = depositJpaRepository.save(deposit);
			BigDecimal afterBalance = savedDeposit.getBalance();

			// 로그 생성 (충전)
			DepositLog depositLog = DepositLog.create(
				userId,
				savedDeposit,
				TransactionType.CHARGE,
				request.amount(),
				beforeBalance,
				afterBalance
			);
			depositLogJpaRepository.save(depositLog);

			return DepositConfirmResponse.from(
				request.orderId(),
				userId,
				approve.paymentKey(),
				request.amount(),
				approve.currency(),
				approve.approvedAt()
			);

		} catch (Exception e) {
			throw new CustomException(DepositExceptionCode.DEPOSIT_FAILD.getMessage());
		}

	}
}
