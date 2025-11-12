package com.domainservice.domain.post.post.exception;

import com.common.exception.CustomException;
import com.common.exception.vo.ProductPostExceptionCode;
import lombok.Getter;

/**
 * 상품 게시글 관련 예외
 */
@Getter
public class ProductPostException extends CustomException {

    private final int code;

    public ProductPostException(ProductPostExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.code = exceptionCode.getCode();
    }

}