package com.domainservice.domain.payment.model.dto;

import com.domainservice.domain.payment.model.entity.PaymentRefundEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.List;

import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record RefundResponse(
    String mId,
    String lastTransactionKey,
    String paymentKey,
    String orderId,
    String orderName,
    Integer taxExemptionAmount,
    String status,
    OffsetDateTime requestedAt,
    OffsetDateTime approvedAt,
    Boolean useEscrow,
    Boolean cultureExpense,
    CardInfo card,
    Object virtualAccount,
    Object transfer,
    Object mobilePhone,
    Object giftCertificate,
    Object cashReceipt,
    Object cashReceipts,
    Object discount,
    List<CancelInfo> cancels,
    Object secret,
    String type,
    EasyPayInfo easyPay,
    String country,
    Object failure,
    Boolean isPartialCancelable,
    ReceiptInfo receipt,
    CheckoutInfo checkout,
    String currency,
    Integer totalAmount,
    Integer balanceAmount,
    Integer suppliedAmount,
    Integer vat,
    Integer taxFreeAmount,
    String method,
    String version,
    Object metadata,
    OffsetDateTime canceledAt
) {
    public static RefundResponse from(PaymentRefundEntity paymentRefund) {
        return RefundResponse.builder()
            .paymentKey(paymentRefund.getPayment().getPaymentKey())
            .orderId(paymentRefund.getPayment().getOrder().getId())
            .cancels(
                List.of(
                    CancelInfo.builder()
                        .cancelAmount(paymentRefund.getCancelAmount().intValue())
                        .cancelReason(paymentRefund.getCancelReason())
                        .build()
                ))
            .status(paymentRefund.getStatus().toString())
            .approvedAt(paymentRefund.getApprovedAt())
            .canceledAt(paymentRefund.getCanceledAt())
            .build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CardInfo(
        String issuerCode,
        String acquirerCode,
        String number,
        Integer installmentPlanMonths,
        Boolean isInterestFree,
        String interestPayer,
        String approveNo,
        Boolean useCardPoint,
        String cardType,
        String ownerType,
        String acquireStatus,
        Integer amount
    ) {
    }

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CancelInfo(
        String transactionKey,
        String cancelReason,
        Integer taxExemptionAmount,
        OffsetDateTime canceledAt,
        Integer transferDiscountAmount,
        Integer easyPayDiscountAmount,
        String receiptKey,
        Integer cancelAmount,
        Integer taxFreeAmount,
        Integer refundableAmount,
        String cancelStatus,
        String cancelRequestId
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EasyPayInfo(
        String provider,
        Integer amount,
        Integer discountAmount
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReceiptInfo(
        String url
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CheckoutInfo(
        String url
    ) {
    }
}
