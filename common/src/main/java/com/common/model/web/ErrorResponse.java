package com.common.model.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;


/**
 * 에러 응답 DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int code,
        String message,
        List<CustomFieldError> errors
) {
    /**
     * code와 message로 생성 (errors 없음)
     */
    public static ErrorResponse of(int code, String message) {
        return new ErrorResponse(code, message, null);
    }

    /**
     * Validation 에러로 생성 (errors 포함)
     */
    public static ErrorResponse of(int code, String message, BindingResult bindingResult) {
        return new ErrorResponse(
                code,
                message,
                CustomFieldError.of(bindingResult)
        );
    }

    /**
     * 필드 에러 정보
     */
    public record CustomFieldError(
            String field,
            String rejectedValue,
            String reason
    ) {
        /**
         * BindingResult로부터 필드 에러 목록 생성
         */
        public static List<CustomFieldError> of(BindingResult bindingResult) {

            List<FieldError> fieldErrors = bindingResult.getFieldErrors();

            return fieldErrors.stream()
                    .map(error -> new CustomFieldError(
                            error.getField(),
                            error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                            error.getDefaultMessage()
                    ))
                    .toList();
        }
    }
}