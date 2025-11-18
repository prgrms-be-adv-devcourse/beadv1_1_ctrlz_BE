package com.domainservice.domain.payment.model.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.common.model.persistence.BaseEntity;
import com.domainservice.domain.payment.model.enums.PaymentStatus;

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
@Table(name = "refund")
public class PaymentRefundEntity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", unique = true) // unique = true로 1:1 보장
    private PaymentEntity payment;

    @Column
    private String paymentKey;

    @Column
    private String orderId;

    @Column(nullable = false)
    private BigDecimal cancelAmount;

    @Column(nullable = false)
    private String cancelReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private OffsetDateTime approvedAt; // 원 결제 승인 시각
    private OffsetDateTime canceledAt;

    // 토스 환불 성공 시 status, canceledAt 업데이트
    public void refundSuccess(String orderId, OffsetDateTime canceledAt) {
        this.orderId = orderId;
        this.status = PaymentStatus.REFUNDED;
        this.canceledAt = canceledAt;
    }

    // 환불 실패 시 status 업데이트
    public void refundFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public void linkPayment(PaymentEntity payment) {
        this.payment = payment;
    }

    @Override
    protected String getEntitySuffix() {
        return "refund";
    }

    public static PaymentRefundEntity of(String paymentKey, String orderId, BigDecimal cancelAmount,
        String cancelReason, PaymentStatus status, OffsetDateTime approvedAt, OffsetDateTime canceledAt) {
        return PaymentRefundEntity.builder()
            .paymentKey(paymentKey)
            .orderId(orderId)
            .cancelAmount(cancelAmount)
            .cancelReason(cancelReason)
            .status(status)
            .approvedAt(approvedAt)
            .canceledAt(canceledAt)
            .build();
    }
}
