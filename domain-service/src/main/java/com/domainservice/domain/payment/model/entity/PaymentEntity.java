package com.domainservice.domain.payment.model.entity;

import java.time.LocalDateTime;

import com.common.model.persistence.BaseEntity;
import com.domainservice.domain.payment.model.enums.PayType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "payments")
public class PaymentEntity extends BaseEntity {

    /** 사용자 서비스에서 받아올 ID */
    @Column(name = "users_id", nullable = false)
    private String usersId;

    /** Toss 결제키 */
    @Column(nullable = false, unique = true)
    private String paymentKey;

    /** 주문 서비스에서 받아올 ID */
    @Column(name = "orders_id", nullable = false)
    private String orderId;

    /** 요청 금액 Order에서 넘어온 금액 */
    @Column(nullable = false)
    private int requestedAmount;

    /** 실제 결제된 금액*/
    @Column(nullable = false)
    private int amount;

    /** 예치금 사용 금액 */
    @Column(nullable = false)
    private int depositUsedAmount;

    /** Toss 충전된 금액 */
    @Column(nullable = false)
    private int tossChargedAmount;

    /** 결제 통화 */
    @Column(nullable = false)
    private String currency;

    /** 결제 수단 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayType payType;

    /** 상태 */
    @Column(nullable = false)
    private String status;

    /** 실패 사유 */
    @Column
    private String failureReason;

    /** 결제 승인 시각 */
    private LocalDateTime approvedAt;

    @Override
    protected String getEntitySuffix() {
        return "payment";
    }

    /** 실제 결제 금액 계산 후 빌더에서 반환 */
    public static PaymentEntity of(String userId, String orderId, int requestedAmount,
        int depositUsedAmount, int tossChargedAmount,
        String currency, PayType payType,
        String status, String paymentKey,
        String failureReason, LocalDateTime approvedAt) {
        int actualAmount = depositUsedAmount + tossChargedAmount;
        return PaymentEntity.builder()
            .usersId(userId)
            .orderId(orderId)
            .requestedAmount(requestedAmount)
            .amount(actualAmount)
            .depositUsedAmount(depositUsedAmount)
            .tossChargedAmount(tossChargedAmount)
            .currency(currency)
            .payType(payType)
            .status(status)
            .paymentKey(paymentKey)
            .failureReason(failureReason)
            .approvedAt(approvedAt)
            .build();
    }
}
