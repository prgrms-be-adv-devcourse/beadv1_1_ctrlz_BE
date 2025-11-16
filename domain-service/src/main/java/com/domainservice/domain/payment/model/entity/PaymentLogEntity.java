package com.domainservice.domain.payment.model.entity;

import java.time.LocalDateTime;

import com.common.model.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
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
@Table(name = "payment_logs")
public class PaymentLogEntity extends BaseEntity {

    @Column(name = "orders_id")
    private String orderId;

    @Column(name = "users_id")
    private String usersId;

    @Column
    private String paymentKey;

    // SUCCESS, FAIL, REQUEST
    @Column(nullable = false)
    private String status;

    // PG 요청 전문 JSON 그대로 저장
    @Lob
    private String requestBody;

    // PG 응답 전문 JSON 그대로 저장
    @Lob
    private String responseBody;

    @Column(length = 1000)
    private String failReason;

    @Column(nullable = false)
    private LocalDateTime loggedAt;

    @Override
    protected String getEntitySuffix() {
        return "payment_logs";
    }
}
