package com.domainservice.domain.post.post.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TradeStatus {
    SELLING("판매중"),
    PROCESSING("거래중"),
    SOLDOUT("판매완료");

    private final String description;
}