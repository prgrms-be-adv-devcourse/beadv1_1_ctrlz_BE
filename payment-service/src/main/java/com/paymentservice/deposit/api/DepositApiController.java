package com.paymentservice.deposit.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.paymentservice.deposit.model.dto.DepositConfirmRequest;
import com.paymentservice.deposit.model.dto.DepositConfirmResponse;
import com.paymentservice.deposit.model.dto.DepositResponse;
import com.paymentservice.deposit.model.entity.Deposit;
import com.paymentservice.deposit.service.DepositService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/deposits")
@RequiredArgsConstructor
public class DepositApiController {
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

	@PostMapping("/confirm")
	public BaseResponse<DepositConfirmResponse> confirmDeposit(
		@RequestBody DepositConfirmRequest request
		// @RequestHeader(value = "X-REQUEST-ID") String userId
	) {
		try {
			String userId = "user-001";
			DepositConfirmResponse response = depositService.tossPayment(request, userId);
			return new BaseResponse<>(response, "충전 완료");
		} catch (Exception e) {
			log.error("충전 중 오류", e);
			return new BaseResponse<>(null, "충전 실패: " + e.getMessage());
		}
	}


}
