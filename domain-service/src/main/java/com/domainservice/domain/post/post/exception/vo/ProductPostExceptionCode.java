package com.domainservice.domain.post.post.exception.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductPostExceptionCode {

    // ===== 400 Bad Request =====
    IMAGE_REQUIRED(400, "이미지는 최소 1개 이상 첨부해야 합니다."),
    TOO_MANY_IMAGES(400, "이미지는 최대 10개까지 등록 가능합니다."),

    // ===== 401 Unauthorized =====
    UNAUTHORIZED(401, "로그인된 회원만 이용 가능합니다."),

    // ===== 403 Forbidden =====
    PRODUCT_POST_FORBIDDEN(403, "자신이 작성한 글만 삭제 가능합니다."),
    PRODUCT_POST_IN_PROGRESS(403, "거래중인 글은 삭제할 수 없습니다."),

    // ===== 404 Not Found =====
    TAG_NOT_FOUND(404, "존재하지 않는 태그가 포함되어있습니다."),
    PRODUCT_POST_NOT_FOUND(404, "해당 글은 존재하지 않습니다."),
    PRODUCT_POST_DELETED(404, "해당 게시글은 삭제되어 조회할 수 없습니다."),

    // ===== 409 Conflict =====
    ALREADY_DELETED(409, "이미 삭제된 상품입니다."),
    CANNOT_UPDATE_SOLDOUT(409, "판매 완료된 상품은 수정할 수 없습니다.");

    private final int code;
    private final String message;

}