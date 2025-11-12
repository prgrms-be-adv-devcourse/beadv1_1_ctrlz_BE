package com.domainservice.domain.payment.model.enums;

public enum PayType {
    DEPOSIT,          // 예치금만 사용
    TOSS,             // 토스페이먼츠만 사용
    DEPOSIT_TOSS      // 예치금 일부 + 토스페이먼츠
}
