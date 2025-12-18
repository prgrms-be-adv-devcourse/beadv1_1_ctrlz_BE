package com.paymentservice.payment.exception;

import org.springframework.http.HttpStatus;

import com.common.exception.CustomException;
import com.common.exception.vo.PaymentExceptionCode;

import lombok.Getter;

@Getter
public abstract class PaymentException extends CustomException {

    private final HttpStatus status;

    public PaymentException(PaymentExceptionCode code) {
        super(code.getMessage());
        this.status = code.getStatus();
    }
}
