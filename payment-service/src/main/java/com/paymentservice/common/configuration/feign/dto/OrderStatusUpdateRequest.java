package com.paymentservice.common.configuration.feign.dto;

import com.paymentservice.common.model.order.OrderStatus;
import com.paymentservice.payment.model.entity.PaymentEntity;

public record OrderStatusUpdateRequest(
    OrderStatus orderStatus,
    String paymentId
) {
    public static OrderStatusUpdateRequest of(OrderStatus status, PaymentEntity paymentEntity){
        return new OrderStatusUpdateRequest(
            status,
            String.valueOf(paymentEntity.getId())
        );

    }
}
