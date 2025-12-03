package com.common.exception.vo;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaymentExceptionCode {

    INVALID_ORDER_AMOUNT(HttpStatus.BAD_REQUEST, "유효하지 않은 결제 금액입니다."),
    PAYMENT_GATEWAY_FAILED(HttpStatus.BAD_GATEWAY, "결제 게이트웨이 요청 중 오류가 발생했습니다."),
    PAYMENT_FAILED(HttpStatus.BAD_GATEWAY, "결제 처리 중 오류가 발생했습니다." ),
    REFUND_FAILD(HttpStatus.BAD_GATEWAY, "환불 처리 중 오류가 발생했습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자의 결제 정보가 존재하지 않습니다.");

    private final HttpStatus status;
    private final String message;

}
