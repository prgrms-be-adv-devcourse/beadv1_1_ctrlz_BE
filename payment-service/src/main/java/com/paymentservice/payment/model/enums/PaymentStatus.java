package com.paymentservice.payment.model.enums;

public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    CANCELED,
    REFUNDED,
    PAYMENT_COMPLETED, // kafka event
    REFUND_AFTER_PAYMENT  // kafka event
}
