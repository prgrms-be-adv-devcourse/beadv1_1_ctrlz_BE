package com.settlement.common.exception;

import lombok.Getter;

@Getter
public class SettlementException extends RuntimeException {

    private final String message;

    public SettlementException(String message) {
        super(message);
        this.message = message;
    }

    public SettlementException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }
}
