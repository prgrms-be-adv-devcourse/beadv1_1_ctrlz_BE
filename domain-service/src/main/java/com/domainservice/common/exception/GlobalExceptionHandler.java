package com.domainservice.common.exception;


import com.common.exception.CustomException;
import com.common.model.web.ErrorResponse;
import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.reivew.exception.ReviewException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    /**
     * CustomException를 상속받는 모든 예외 처리
     * 일단 모든 예외를 404 처리함
     * TODO: CustomException에 int code 필드 생성하는게 좋을지?
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {

        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse response = ErrorResponse.of(status.value(), e.getMessage());

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(ReviewException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(ReviewException e) {

        HttpStatus status = e.getStatus();
        ErrorResponse response = ErrorResponse.of(status.value(), e.getMessage());

        return ResponseEntity.status(status).body(response);
    }

    /**
     * CustomException를 상속받는 모든 예외 처리
     */
    @ExceptionHandler(ProductPostException.class)
    public ResponseEntity<ErrorResponse> handleProductPostException(ProductPostException e) {

        HttpStatus status = HttpStatus.valueOf(e.getCode());
        ErrorResponse response = ErrorResponse.of(e.getCode(), e.getMessage());

        return ResponseEntity.status(status).body(response);
    }

    /**
     * Validation 예외 처리 (400)
     * 입력값이 잘못 들어온 경우
     * 해당 입력값의 field, rejectedValue, reason을 확인할 수 있음
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e) {

        // ErrorResponse 생성
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "입력값 검증에 실패했습니다.",
                e.getBindingResult()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);

    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoApiException(NoResourceFoundException e) {

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value()
                , "요청하신 API 엔드포인트를 찾을 수 없습니다."
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * 기타 걸러지지 않은 오류 발생 시 (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value()
                , "서버 내부 오류가 발생했습니다."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);

    }

}