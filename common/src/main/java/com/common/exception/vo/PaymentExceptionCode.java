package com.common.exception.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaymentExceptionCode {

    INVALID_ORDER_AMOUNT(400, "유효하지 않은 결제 금액입니다."),
    PAYMENT_GATEWAY_FAILED(502, "결제 게이트웨이 요청 중 오류가 발생했습니다."),
    DEPOSIT_NOT_FOUND(404, "해당 사용자의 예치금 정보가 존재하지 않습니다."),
    ORDER_NOT_FOUND(404, "해당 사용자의 주문 정보가 존재하지 않습니다."),
    INVALID_USER_ID(400, "유효하지 않은 사용자 ID입니다."),
    PAYMENT_FAILED(502, "결제 처리 중 오류가 발생했습니다." ),
    REFUND_FAILD(502, "환불 처리 중 오류가 발생했습니다."),
    PAYMENT_NOT_FOUND(404, "해당 사용자의 결제 정보가 존재하지 않습니다.");

    private final int code;
    private final String message;

    public String addErrorInMessage(String error) {
        return this.message + " : " + error;
    }
}
