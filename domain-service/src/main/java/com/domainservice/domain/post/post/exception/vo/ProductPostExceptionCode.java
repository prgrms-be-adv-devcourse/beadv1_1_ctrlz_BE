package com.domainservice.domain.post.post.exception.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductPostExceptionCode {

    TAG_NOT_FOUND(404, "존재하지 않는 태그가 포함되어있습니다.");

    private final int code;
    private final String message;

}