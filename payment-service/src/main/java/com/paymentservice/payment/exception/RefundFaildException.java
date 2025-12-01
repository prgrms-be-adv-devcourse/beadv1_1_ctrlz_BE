package com.paymentservice.payment.exception;

import com.common.exception.vo.PaymentExceptionCode;

public class RefundFaildException extends PaymentException {
    public RefundFaildException() {
        super(PaymentExceptionCode.REFUND_FAILD);
    }
}
