package com.common.model.web;

import com.common.exception.CustomException;

public record ErrorResponse(
        int code,
        String message
) {
    public static ErrorResponse from(CustomException e) {
        return new ErrorResponse(
                e.getCode(),
                e.getMessage()
        );
    }
}