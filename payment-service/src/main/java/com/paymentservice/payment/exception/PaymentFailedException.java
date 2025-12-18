package com.paymentservice.payment.exception;

import com.common.exception.vo.PaymentExceptionCode;

public class PaymentFailedException extends PaymentException {
    public PaymentFailedException() {
        super(PaymentExceptionCode.PAYMENT_FAILED);
    }
}
