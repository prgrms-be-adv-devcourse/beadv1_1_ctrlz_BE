package com.domainservice;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.common.exception.CustomException;
import com.domainservice.domain.deposit.model.dto.DepositResponse;
import com.domainservice.domain.deposit.model.entity.Deposit;
import com.domainservice.domain.deposit.model.entity.DepositLog;
import com.domainservice.domain.deposit.repository.DepositJpaRepository;
import com.domainservice.domain.deposit.repository.DepositLogJpaRepository;
import com.domainservice.domain.deposit.service.DepositService;

@ExtendWith(MockitoExtension.class)
class DepositServiceTest {

	@Mock
	private DepositJpaRepository depositJpaRepository;

	@Mock
	private DepositLogJpaRepository depositLogJpaRepository;

	@InjectMocks
	private DepositService depositService;

	private String userId;
	private Deposit deposit;

	@BeforeEach
	void setUp() {
		userId = "testUser";
		deposit = Deposit.builder()
			.userId(userId)
			.balance(BigDecimal.valueOf(1000))
			.build();
	}

	@Test
	@DisplayName("사용자 ID로 예치금 조회 - 존재하는 경우")
	void test1() {
		// given
		when(depositJpaRepository.findByUserId(userId)).thenReturn(Optional.of(deposit));

		// when
		Deposit result = depositService.getDepositByUserId(userId);

		// then
		assertThat(result).isEqualTo(deposit);
		verify(depositJpaRepository).findByUserId(userId);
	}

	@Test
	@DisplayName("사용자 예치금 충전")
	void test2() {
		// given
		BigDecimal amount = BigDecimal.valueOf(500);
		when(depositJpaRepository.findByUserId(userId)).thenReturn(Optional.of(deposit));
		when(depositJpaRepository.save(deposit)).thenReturn(deposit);
		when(depositLogJpaRepository.save(any(DepositLog.class))).thenAnswer(i -> i.getArgument(0));

		// when
		DepositResponse response = depositService.chargeDeposit(userId, amount);

		// then
		assertThat(response.balance()).isEqualTo(BigDecimal.valueOf(1500));
		assertThat(response.message()).isEqualTo("충전이 완료되었습니다.");
		verify(depositJpaRepository).save(deposit);
		verify(depositLogJpaRepository).save(any(DepositLog.class));
	}

	@Test
	@DisplayName("사용자 예치금 사용")
	void test3() {
		// given
		BigDecimal amount = BigDecimal.valueOf(500);
		when(depositJpaRepository.findByUserId(userId)).thenReturn(Optional.of(deposit));
		when(depositJpaRepository.save(deposit)).thenReturn(deposit);
		when(depositLogJpaRepository.save(any(DepositLog.class))).thenAnswer(i -> i.getArgument(0));

		// when
		DepositResponse response = depositService.useDeposit(userId, amount);

		// then
		assertThat(response.balance()).isEqualTo(BigDecimal.valueOf(500));
		assertThat(response.message()).isEqualTo("예치금 사용이 완료되었습니다.");
		verify(depositJpaRepository).save(deposit);
		verify(depositLogJpaRepository).save(any(DepositLog.class));
	}

	@Test
	@DisplayName("잔액 조회")
	void test4() {
		// given
		when(depositJpaRepository.findByUserId(userId)).thenReturn(Optional.of(deposit));

		// when
		DepositResponse response = depositService.getDepositBalance(userId);

		// then
		assertThat(response.balance()).isEqualTo(BigDecimal.valueOf(1000));
		assertThat(response.message()).isEqualTo("잔액 조회가 완료되었습니다.");
	}

	@Test
	@DisplayName("예치금 충분 여부 확인")
	void test5() {
		// given
		when(depositJpaRepository.findByUserId(userId)).thenReturn(Optional.of(deposit));

		// when
		boolean enough = depositService.hasEnoughDeposit(userId, BigDecimal.valueOf(500));
		boolean notEnough = depositService.hasEnoughDeposit(userId, BigDecimal.valueOf(1500));

		// then
		assertThat(enough).isTrue();
		assertThat(notEnough).isFalse();
	}

	@Test
	@DisplayName("잔액 부족 시 예치금 사용 예외 발생")
	void test6() {
		// given
		BigDecimal useAmount = BigDecimal.valueOf(2000); // 현재 잔액 1000
		when(depositJpaRepository.findByUserId(userId)).thenReturn(Optional.of(deposit));

		// when & then
		assertThatThrownBy(() -> depositService.useDeposit(userId, useAmount))
			.isInstanceOf(CustomException.class);
	}
}
