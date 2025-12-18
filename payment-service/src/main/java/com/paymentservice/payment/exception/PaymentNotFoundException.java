package com.paymentservice.payment.exception;

import com.common.exception.vo.PaymentExceptionCode;

public class PaymentNotFoundException extends PaymentException {
    public PaymentNotFoundException() {
        super(PaymentExceptionCode.PAYMENT_NOT_FOUND);
    }
}
