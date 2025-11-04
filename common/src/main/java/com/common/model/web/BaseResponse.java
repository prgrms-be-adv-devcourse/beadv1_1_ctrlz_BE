package com.common.model.web;

public record BaseResponse<T>(
        T data,
        String message
) {
}