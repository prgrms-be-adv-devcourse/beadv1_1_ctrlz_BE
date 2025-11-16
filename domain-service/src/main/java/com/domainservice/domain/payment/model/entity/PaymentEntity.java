package com.domainservice.domain.payment.model.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.common.model.persistence.BaseEntity;
import com.domainservice.domain.order.model.entity.Order;
import com.domainservice.domain.payment.model.enums.PayType;
import com.domainservice.domain.payment.model.enums.PaymentStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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

    @Column(name = "users_id", nullable = false)
    private String usersId;

    // 토스에서 자동 발급될 키
    @Column
    private String paymentKey;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_id", nullable = false)
    private Order order;

    // order에서 넘어오는 요청 금액
    @Column(nullable = false)
    private BigDecimal requestedAmount;

    // 실제 결제된 금액
    @Column(nullable = false)
    private BigDecimal amount;

    // 예치금 사용 금액
    @Column(nullable = false)
    private BigDecimal depositUsedAmount;

    // 토스 충전된 금액
    @Column(nullable = false)
    private BigDecimal tossChargedAmount;

    @Column(nullable = false)
    private String currency;

    // 결제 수단(예치금/예치금+토스/토스)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayType payType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private OffsetDateTime approvedAt;

    @OneToOne(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PaymentRefundEntity refund;

    public void linkRefund(PaymentRefundEntity refund) {
        this.refund = refund;
        refund.linkPayment(this);
        this.updateStatus(PaymentStatus.REFUNDED);
    }

    public void updateStatus(PaymentStatus newStatus) {
        this.status = newStatus;
    }

    public void linkOrder(Order order) {
        this.order = order;
        order.setPayment(this);
    }

    @Override
    protected String getEntitySuffix() {
        return "payment";
    }

    public static PaymentEntity of(String userId, Order order, BigDecimal requestedAmount,
        BigDecimal depositUsedAmount, BigDecimal tossChargedAmount,
        String currency, PayType payType,
        PaymentStatus status, String paymentKey, OffsetDateTime approvedAt) {
        BigDecimal actualAmount = depositUsedAmount.add(tossChargedAmount);
        return PaymentEntity.builder()
            .usersId(userId)
            .order(order)
            .requestedAmount(requestedAmount)
            .amount(actualAmount)
            .depositUsedAmount(depositUsedAmount)
            .tossChargedAmount(tossChargedAmount)
            .currency(currency)
            .payType(payType)
            .status(status)
            .paymentKey(paymentKey)
            .approvedAt(approvedAt)
            .build();
    }
}
