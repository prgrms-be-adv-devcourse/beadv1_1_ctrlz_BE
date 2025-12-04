package com.paymentservice.deposit.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.event.SettlementReadyEvent;
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
				() -> depositJpaRepository.save(Deposit.builder().userId(userId).balance(BigDecimal.ZERO).build()));
	}

	/**
	 * 사용자의 예치금 충전
	 */
	// TODO: 상아 토스 페이먼츠 API 수정
	public DepositResponse chargeDeposit(String userId, BigDecimal amount) {
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new CustomException(DepositExceptionCode.INVALID_AMOUNT.getMessage());
		}

		Deposit deposit = getDepositByUserId(userId);
		BigDecimal beforeBalance = deposit.getBalance();
		deposit.increaseBalance(amount);
		BigDecimal afterBalance = deposit.getBalance();

		// TODO: 토스 페이먼츠 타기


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
	public Deposit getDepositBalance(String userId) {
		return getDepositByUserId(userId);
	}


	/**
	 * 정산 준비로 넘어온 정산 아이템 예치금에 반영
	 */
	public void processSettlement(SettlementReadyEvent event) {
		// 이미 처리한 settlement면 종료. 정합성 보장
		if (depositLogJpaRepository.existsByTransactionTypeAndReferenceId(
			TransactionType.SETTLEMENT, event.settlementId())) {
			return;
		}
		Deposit deposit = depositJpaRepository.findByUserId(event.userId())
			.orElseThrow(() -> new CustomException(DepositExceptionCode.DEPOSIT_NOT_FOUND.getMessage()));

		BigDecimal amount = event.netAmount();
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new CustomException(DepositExceptionCode.INVALID_AMOUNT.getMessage());
		}
		BigDecimal before = deposit.getBalance();

		deposit.increaseBalance(amount);

		BigDecimal after = deposit.getBalance();

		// 로그 생성
		DepositLog log = DepositLog.createBySettlementReady(
			event.userId(),
			deposit,
			TransactionType.SETTLEMENT,
			amount,
			before,
			after,
			event.settlementId()
		);

		depositLogJpaRepository.save(log);
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

	public void markSettlementFailed(SettlementReadyEvent event, String reason) {
		// 이미 성공 로그가 있으면 실패 로그는 굳이 안 남김
		if (depositLogJpaRepository.existsByTransactionTypeAndReferenceId(
			TransactionType.SETTLEMENT, event.settlementId())) {
			return;
		}

		try {
			Deposit deposit = depositJpaRepository.findByUserId(event.userId())
				.orElse(null);

			// deposit 자체가 없어서 실패한 케이스
			if (deposit == null) {
				// 예치금 계좌 없음 → 예치금 로그는 만들지 않고 애플리케이션 로그만
				log.warn("정산 실패 - 예치금 계좌 없음 settlementId={}, userId={}, reason={}",
					event.settlementId(), event.userId(), reason);
				return;
			}

			BigDecimal beforeBalance = deposit.getBalance();

			DepositLog depositLog = DepositLog.createBySettlementReady(
				event.userId(),
				deposit, // ★ 여기서는 null 아님
				TransactionType.SETTLEMENT_FAIL,
				BigDecimal.ZERO,
				beforeBalance,
				beforeBalance,
				event.settlementId()
			);

			depositLogJpaRepository.save(depositLog);

			log.warn(
				"정산 예치금 처리 비즈니스 실패 로그 기록 settlementId={}, userId={}, reason={}",
				event.settlementId(), event.userId(), reason
			);
		} catch (Exception ex) {
			// TODO 실패 로그 저장 자체가 터져도 정산 흐름까지 막고 싶진 않으니 여기서만 잡고 끝
			log.error(
				"정산 예치금 실패 로그 저장 중 오류 settlementId={}, userId={}",
				event.settlementId(), event.userId(), ex
			);
		}
	}


	public Deposit createDeposit(String userId) {

		if(depositJpaRepository.findByUserId(userId).isPresent()){
			throw new CustomException(DepositExceptionCode.DEPOSIT_ALREADY_EXISTS.getMessage());
		}

		Deposit deposit = Deposit.builder()
			.userId(userId)
			.balance(BigDecimal.valueOf(0L))
			.build();

		return depositJpaRepository.save(deposit);
	}

	/**
	 * Toss 결제 확정 처리
	 * - Toss 승인을 받고 (DepositTossClient.approve)
	 * - 승인된 금액과 요청 금액을 검증 (불일치 시 예외)
	 * - deposit balance 증가, paymentKey 저장, 로그 생성
	 */
	public DepositConfirmResponse tossPayment(DepositConfirmRequest request, String userId) {
		try {
			// toss payments api
			TossChargeResponse approve = depositTossClient.approve(userId, request);

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
