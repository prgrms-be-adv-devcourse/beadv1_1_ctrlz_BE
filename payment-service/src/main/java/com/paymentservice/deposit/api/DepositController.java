package com.paymentservice.deposit.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.paymentservice.deposit.model.dto.DepositResponse;
import com.paymentservice.deposit.model.entity.Deposit;
import com.paymentservice.deposit.service.DepositService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/deposits")
@RequiredArgsConstructor
public class DepositController {
	private final DepositService depositService;


	@PostMapping
	public BaseResponse<DepositResponse> createDeposit(
		@RequestHeader(value = "X-REQUEST-ID") String userId
	) {
		Deposit deposit = depositService.createDeposit(userId);
		DepositResponse response = DepositResponse.from(deposit);

		return new BaseResponse<>(response, "예치금 생성 성공");
	}

	@GetMapping
	public BaseResponse<DepositResponse> getDeposit(
		@RequestHeader(value = "X-REQUEST-ID") String userId
	) {
		Deposit deposit = depositService.getDepositBalance(userId);
		DepositResponse response = DepositResponse.from(deposit);

		return new BaseResponse<>(response, "예치금 조회 성공");
	}
}
