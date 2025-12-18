package com.paymentservice.payment.exception;

import com.common.exception.vo.PaymentExceptionCode;

public class InvalidOrderAmountException extends PaymentException {
    public InvalidOrderAmountException() {
        super(PaymentExceptionCode.INVALID_ORDER_AMOUNT);
    }
}
