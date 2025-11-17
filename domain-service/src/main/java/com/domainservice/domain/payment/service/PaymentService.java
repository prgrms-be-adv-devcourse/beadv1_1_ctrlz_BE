package com.domainservice.domain.payment.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.exception.CustomException;
import com.common.exception.vo.PaymentExceptionCode;
import com.domainservice.domain.deposit.model.entity.Deposit;
import com.domainservice.domain.deposit.repository.DepositJpaRepository;
import com.domainservice.domain.deposit.service.DepositService;
import com.domainservice.domain.order.model.entity.Order;
import com.domainservice.domain.order.repository.OrderRepository;
import com.domainservice.common.configuration.feign.client.PaymentFeignClient;
import com.domainservice.domain.payment.model.dto.PaymentConfirmRequest;
import com.domainservice.domain.payment.model.dto.PaymentReadyResponse;
import com.domainservice.domain.payment.model.dto.PaymentResponse;
import com.domainservice.domain.payment.model.dto.RefundResponse;
import com.domainservice.domain.payment.model.entity.PaymentEntity;
import com.domainservice.domain.payment.model.entity.PaymentRefundEntity;
import com.domainservice.domain.payment.model.enums.PayType;
import com.domainservice.domain.payment.model.enums.PaymentStatus;
import com.domainservice.domain.payment.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final DepositService depositService;
    private final DepositJpaRepository depositJpaRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentFeignClient paymentFeignClient;
    private final OrderRepository orderRepository;
    private final PaymentLogService paymentLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${custom.payment.toss.test_secrete_api_key}")
    private String secretApiKey;

    @Transactional
    public PaymentReadyResponse getPaymentReadyInfo(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new CustomException(PaymentExceptionCode.ORDER_NOT_FOUND.getMessage()));

        BigDecimal orderAmount = order.getTotalAmount();
        if (orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(PaymentExceptionCode.INVALID_ORDER_AMOUNT.getMessage());
        }

        String orderName = order.getOrderName();

        Deposit deposit = depositJpaRepository.findByUserId(order.getBuyerId())
            .orElseThrow(() -> new CustomException(PaymentExceptionCode.DEPOSIT_NOT_FOUND.getMessage()));

        BigDecimal depositBalance = deposit.getBalance();

        return new PaymentReadyResponse(
            order.getBuyerId(),
            orderId,
            orderAmount,
            depositBalance,
            orderName
        );
    }

    /** deposit으로만 결제 할 경우 */
    @Transactional
    public PaymentResponse depositPayment(PaymentConfirmRequest request) {
        String status = "FAIL";
        String failReason = null;
        String requestJson = null;
        String userId = null;

        try {
            Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new CustomException(PaymentExceptionCode.ORDER_NOT_FOUND.getMessage()));
            userId = order.getBuyerId();

            requestJson = objectMapper.writeValueAsString(request);

            // Toss 로그 기록(요청 전문): REQUEST상태
            paymentLogService.saveLog(
                request.orderId(),
                userId,
                request.paymentKey(),
                "REQUEST",
                requestJson,
                null,
                null
            );

            // 예치금 차감
            depositService.useDeposit(order.getBuyerId(), request.usedDepositAmount());
            BigDecimal orderTotalAmount = order.getTotalAmount();

            // 주문 상태 변경
            order.orderConfirmed();

            // 실제 결제 총액 검증
            if (orderTotalAmount.compareTo(request.usedDepositAmount()) != 0) {
                throw new CustomException(PaymentExceptionCode.INVALID_ORDER_AMOUNT.getMessage());
            }

            PaymentEntity paymentEntity;
            paymentEntity = PaymentEntity.of(order.getBuyerId(), order, orderTotalAmount, request.amount(),
                BigDecimal.ZERO, "KRW", PayType.DEPOSIT, PaymentStatus.SUCCESS, null, OffsetDateTime.now()
            );

            paymentEntity.linkOrder(order);

            paymentRepository.save(paymentEntity);

            status = "SUCCESS";
            return PaymentResponse.from(paymentEntity);
        } catch (CustomException e) {
            failReason = "CustomException: " + e.getMessage();
            throw e;
        } catch (Exception e) {
            log.error("예치금 결제 처리 오류", e);
            failReason = "Exception: " + e.getMessage();
            throw new CustomException(PaymentExceptionCode.PAYMENT_FAILED.getMessage());
        } finally {
            // 예치금 결제 시도 로그 기록 (성공/실패 모두)
            paymentLogService.saveLog(
                request.orderId(),
                userId,
                null,
                status,
                requestJson,
                null,
                failReason
            );

        }
    }

    /** toss payments 사용 결제 할 경우 */
    @Transactional
    public PaymentResponse processPayment(PaymentConfirmRequest request) {
        String status = "FAIL";
        String requestJson = null;
        String responseJson = null;
        String failReason = null;
        String userId = null;

        try {
            Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new CustomException(PaymentExceptionCode.ORDER_NOT_FOUND.getMessage()));
            userId = order.getBuyerId();

            Deposit deposit = depositJpaRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(PaymentExceptionCode.DEPOSIT_NOT_FOUND.getMessage()));

            BigDecimal usedDepositAmount = request.usedDepositAmount();
            BigDecimal totalAmount = request.totalAmount();

            if (deposit.getBalance().compareTo(BigDecimal.ZERO) > 0
                && usedDepositAmount.compareTo(BigDecimal.ZERO) > 0) {
                // 예치금 차감
                depositService.useDeposit(userId, usedDepositAmount);
            }
            BigDecimal orderTotalAmount = order.getTotalAmount();

            // 실제 결제 총액 검증
            if (orderTotalAmount.compareTo(request.amount()) != 0) {
                throw new CustomException(PaymentExceptionCode.INVALID_ORDER_AMOUNT.getMessage());
            }

            PayType payType = (usedDepositAmount.compareTo(BigDecimal.ZERO) > 0) ? PayType.DEPOSIT_TOSS : PayType.TOSS;

            PaymentEntity paymentEntity;

            String key = (secretApiKey != null) ? secretApiKey : "test_secret_key";
            String authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((key + ":").getBytes(StandardCharsets.UTF_8));

            // Toss로 보내야하는 필수 필드
            Map<String, Object> requestBody = Map.of(
                "paymentKey", request.paymentKey(),
                "orderId", request.orderId(),
                "amount", request.totalAmount()
            );
            requestJson = objectMapper.writeValueAsString(requestBody);

            // Toss 로그 기록(요청 전문): REQUEST상태
            paymentLogService.saveLog(
                request.orderId(),
                userId,
                request.paymentKey(),
                "REQUEST",
                requestJson,
                null,
                null
            );

            Map<String, Object> responseMap = paymentFeignClient.requestPayment(
                requestBody, authHeader
            );

            // 결제 성공처리 및 DB저장
            String responseStatus = (String)responseMap.get("status");
            if (!"DONE".equals(responseStatus)) {
                // Toss 응답 상태가 DONE이 아닌 경우는 실패
                failReason = "Toss Payments 응답 상태가 'DONE'이 아님: " + responseStatus;
                throw new CustomException(PaymentExceptionCode.PAYMENT_GATEWAY_FAILED.getMessage());
            }

            OffsetDateTime approvedAt = null;
            Object approvedAtObj = responseMap.get("approvedAt");
            if (approvedAtObj != null) {
                try {
                    approvedAt = OffsetDateTime.parse(approvedAtObj.toString());
                } catch (DateTimeParseException e) {
                    log.warn("approvedAt 파싱 실패, 현재 시간으로 대체", e);
                    approvedAt = OffsetDateTime.now();
                }
            }

            PaymentStatus paymentStatus = null;
            if ("DONE".equals(responseMap.get("status"))) {
                paymentStatus = PaymentStatus.SUCCESS;
            }
            paymentEntity = PaymentEntity.of(
                userId,
                order,
                request.amount(),
                usedDepositAmount,
                totalAmount,
                (String)responseMap.get("currency"),
                payType,
                paymentStatus,
                (String)responseMap.get("paymentKey"),
                approvedAt
            );
            paymentEntity.linkOrder(order);
            order.orderConfirmed();
            paymentRepository.save(paymentEntity);

            status = "SUCCESS";
            return PaymentResponse.from(paymentEntity);
        } catch (CustomException e) {
            failReason = "CustomException: " + e.getMessage();
            throw e;
        } catch (Exception e) {
            log.error("Toss 결제 승인 실패: {}", e.getMessage());
            // Toss API 통신 오류 등
            failReason = "Exception: " + e.getMessage();
            throw new CustomException(PaymentExceptionCode.PAYMENT_FAILED.getMessage());
        } finally {
            // Toss 최종 응답 로그 기록 (성공/실패 모두)
            paymentLogService.saveLog(
                request.orderId(), userId, request.paymentKey(), status,
                requestJson, responseJson, failReason
            );
        }
    }

    /** 환불 처리 */
    @Transactional
    public RefundResponse refundOrder(PaymentEntity payment, boolean includeDeposit) {
        String status = "FAIL";
        String requestJson = null;
        String responseJson = null;
        String failReason = null;

        PaymentRefundEntity refundEntity = null;

        try {
            BigDecimal depositRefund = payment.getDepositUsedAmount();
            BigDecimal tossRefund = payment.getTossChargedAmount();

            refundEntity = PaymentRefundEntity.builder()
                .paymentKey(payment.getPaymentKey())
                .cancelAmount(depositRefund.add(tossRefund))
                .cancelReason("사용자 요청 환불")
                .status(PaymentStatus.PENDING)
                .approvedAt(payment.getApprovedAt())
                .build();
            payment.linkRefund(refundEntity);

            // 환불 요청 로그 기록
            paymentLogService.saveLog(
                payment.getOrder().getId(),
                payment.getUsersId(),
                payment.getPaymentKey(),
                "REFUND_REQUEST",
                requestJson,
                null,
                null
            );
            Map<String, Object> cancelBody = Map.of(
                "cancelAmount", tossRefund,
                "cancelReason", "사용자 요청 환불"
            );
            requestJson = objectMapper.writeValueAsString(cancelBody);

            // Toss 환불 요청
            if (tossRefund.compareTo(BigDecimal.ZERO) > 0 && payment.getPaymentKey() != null) {
                // 예치금 환불
                if (includeDeposit && depositRefund.compareTo(BigDecimal.ZERO) > 0) {
                    depositService.refundDeposit(payment.getUsersId(), depositRefund);
                }

                String key = (secretApiKey != null) ? secretApiKey : "test_secret_key";
                String authHeader = "Basic " + Base64.getEncoder()
                    .encodeToString((key + ":").getBytes(StandardCharsets.UTF_8));

                Map<String, Object> tossResponse = paymentFeignClient.refundPayment(
                    payment.getPaymentKey(),
                    cancelBody,
                    authHeader
                );

                PaymentRefundEntity paymentRefund = PaymentRefundEntity.of(
                    payment.getPaymentKey(),
                    payment.getOrder().getId(),
                    (BigDecimal)cancelBody.get("cancelAmount"),
                    (String)cancelBody.get("cancelReason"),
                    payment.getStatus(),
                    payment.getApprovedAt(),
                    OffsetDateTime.now()
                );

                responseJson = objectMapper.writeValueAsString(tossResponse);

                refundEntity.refundSuccess(paymentRefund.getOrderId(), OffsetDateTime.now());
            } else if (depositRefund.compareTo(BigDecimal.ZERO) > 0) {
                // 예치금만 결제 환불
                depositService.refundDeposit(payment.getUsersId(), depositRefund);
                refundEntity.refundSuccess(payment.getOrder().getId(), OffsetDateTime.now());
            }
            paymentRepository.save(payment);
            status = "SUCCESS";

            // 주문상태 변경
            Order order = orderRepository.findById(payment.getOrder().getId())
                .orElseThrow(() -> new CustomException(PaymentExceptionCode.ORDER_NOT_FOUND.getMessage()));
            order.orderRefundedAfterPayment();

            return RefundResponse.from(refundEntity);

        } catch (CustomException e) {
            failReason = "CustomException: " + e.getMessage();
            throw e;
        } catch (Exception e) {
            log.error("환불 처리 오류", e);
            failReason = "Exception: " + e.getMessage();
            throw new CustomException(PaymentExceptionCode.REFUND_FAILD.getMessage());
        } finally {
            if (payment != null && refundEntity != null) {
                paymentLogService.saveLog(
                    payment.getOrder().getId(),
                    payment.getUsersId(),
                    payment.getPaymentKey(),
                    "REFUND_" + status,
                    requestJson,
                    responseJson,
                    failReason
                );
            }
        }
    }
}
