package com.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    // private final int code;
    private final String message;

    public CustomException(String message) {
        this.message = message;
    }

    public CustomException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

//    public CustomException(int code, String message) {
//        super(message);
//        this.code = code;
//        this.message = message;
//    }
//
//    public CustomException(int code, String message, Throwable cause) {
//        super(message, cause);
//        this.code = code;
//        this.message = message;
//    }
}