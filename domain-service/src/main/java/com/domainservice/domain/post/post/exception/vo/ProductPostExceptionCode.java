package com.domainservice.domain.post.post.exception.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductPostExceptionCode {

    UNAUTHORIZED(401, "로그인된 회원만 이용 가능합니다."),

    PRODUCT_POST_FORBIDDEN(403, "자신이 작성한 글만 삭제 가능합니다."),
    PRODUCT_POST_IN_PROGRESS(403, "거래중인 글은 삭제할 수 없습니다."),

    TAG_NOT_FOUND(404, "존재하지 않는 태그가 포함되어있습니다."),
    PRODUCT_POST_NOT_FOUND(404, "해당 글은 존재하지 않습니다."),

    ALREADY_DELETED(409, "이미 삭제된 상품입니다.");

    private final int code;
    private final String message;

}