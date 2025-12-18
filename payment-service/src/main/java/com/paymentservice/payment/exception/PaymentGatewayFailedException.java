package com.paymentservice.payment.exception;

import com.common.exception.vo.PaymentExceptionCode;

public class PaymentGatewayFailedException extends PaymentException {
    public PaymentGatewayFailedException() {
        super(PaymentExceptionCode.PAYMENT_GATEWAY_FAILED);
    }
}
