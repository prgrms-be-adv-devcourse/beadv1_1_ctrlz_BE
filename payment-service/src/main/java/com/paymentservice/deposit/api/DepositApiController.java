package com.paymentservice.deposit.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.paymentservice.deposit.docs.ConfirmDepositApiDocs;
import com.paymentservice.deposit.docs.CreateDepositApiDocs;
import com.paymentservice.deposit.docs.GetDepositApiDocs;
import com.paymentservice.deposit.model.dto.DepositConfirmRequest;
import com.paymentservice.deposit.model.dto.DepositConfirmResponse;
import com.paymentservice.deposit.model.dto.DepositResponse;
import com.paymentservice.deposit.model.dto.TossChargeResponse;
import com.paymentservice.deposit.model.entity.Deposit;
import com.paymentservice.deposit.service.DepositService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Deposit", description = "예치금 API")
@Slf4j
@RestController
@RequestMapping("/api/deposits")
@RequiredArgsConstructor
public class DepositApiController {
    private final DepositService depositService;

    @CreateDepositApiDocs
    @PostMapping
    public BaseResponse<DepositResponse> createDeposit(
        @RequestHeader(value = "X-REQUEST-ID") String userId
    ) {
        Deposit deposit = depositService.createDeposit(userId);
        DepositResponse response = DepositResponse.from(deposit);

        return new BaseResponse<>(response, "예치금 생성 성공");
    }

    @GetDepositApiDocs
    @GetMapping
    public BaseResponse<DepositResponse> getDeposit(
        @RequestHeader(value = "X-REQUEST-ID") String userId
    ) {
        Deposit deposit = depositService.getDepositBalance(userId);
        DepositResponse response = DepositResponse.from(deposit);

        return new BaseResponse<>(response, "예치금 조회 성공");
    }

    @ConfirmDepositApiDocs
    @PostMapping("/confirm")
    public BaseResponse<DepositConfirmResponse> confirmDeposit(
        @RequestBody DepositConfirmRequest request,
        @RequestHeader(value = "X-REQUEST-ID") String userId
    ) {
        try {
            TossChargeResponse approve = depositService.tossApprove(request, userId);
            DepositConfirmResponse response = depositService.tossPayment(approve, request, userId);
            return new BaseResponse<>(response, "충전 완료");
        } catch (Exception e) {
            log.error("충전 중 오류", e);
            return new BaseResponse<>(null, "충전 실패: " + e.getMessage());
        }
    }

}
