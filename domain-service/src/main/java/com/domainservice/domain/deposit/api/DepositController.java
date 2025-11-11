package com.domainservice.domain.deposit.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.domainservice.domain.deposit.model.dto.DepositResponse;
import com.domainservice.domain.deposit.service.DepositService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/deposits")
@RequiredArgsConstructor
public class DepositController {
	private final DepositService depositService;
	// TODO
	String USERID = "user-001";

	@GetMapping
	public BaseResponse<DepositResponse> getDeposit() {
		DepositResponse depositBalance = depositService.getDepositBalance(USERID);

		return new BaseResponse<>(depositBalance, "예치금 조회 성공했습니다");
	}
}
