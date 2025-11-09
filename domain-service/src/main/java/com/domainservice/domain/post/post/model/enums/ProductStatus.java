package com.domainservice.domain.post.post.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {
    NEW("새 상품"),
    GOOD("양호"),
    FAIR("보통");

    private final String description;
}