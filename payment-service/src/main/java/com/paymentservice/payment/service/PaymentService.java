package com.paymentservice.payment.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.exception.CustomException;
import com.common.exception.vo.OrderExceptionCode;
import com.paymentservice.common.configuration.feign.client.OrderFeignClient;
import com.paymentservice.common.configuration.feign.dto.OrderStatusUpdateRequest;
import com.paymentservice.common.model.order.OrderResponse;
import com.paymentservice.common.model.order.OrderStatus;
import com.paymentservice.deposit.model.entity.Deposit;
import com.paymentservice.deposit.service.DepositService;
import com.paymentservice.payment.exception.InvalidOrderAmountException;
import com.paymentservice.payment.exception.PaymentFailedException;
import com.paymentservice.payment.exception.PaymentNotFoundException;
import com.paymentservice.payment.model.dto.PaymentConfirmRequest;
import com.paymentservice.payment.model.dto.PaymentReadyResponse;
import com.paymentservice.payment.model.dto.PaymentResponse;
import com.paymentservice.payment.model.dto.RefundResponse;
import com.paymentservice.payment.model.dto.TossApprovalResponse;
import com.paymentservice.payment.model.entity.PaymentEntity;
import com.paymentservice.payment.model.entity.PaymentRefundEntity;
import com.paymentservice.payment.model.enums.PayType;
import com.paymentservice.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentTxService paymentTxService;
    private final OrderFeignClient orderFeignClient;
    private final DepositService depositService;
    private static final Logger log = LoggerFactory.getLogger("API." + PaymentService.class.getName());

    // 결제 정보
    public PaymentReadyResponse getPaymentReadyInfo(String orderId, String userId) {
        OrderResponse order = orderFeignClient.getOrderInfo(orderId, userId);

        if (!Objects.equals(userId, order.buyerId())) {
            throw new CustomException(OrderExceptionCode.ORDER_UNAUTHORIZED.getMessage());
        }

        Deposit deposit = depositService.getDepositBalance(userId);

        return new PaymentReadyResponse(
            userId,
            orderId,
            order.totalAmount(),
            deposit.getBalance(),
            order.orderName()
        );
    }

    // deposit으로만 결제 할 경우
    public PaymentResponse depositPayment(PaymentConfirmRequest request, String userId) {
        OrderResponse order = orderFeignClient.getOrderInfo(request.orderId(), userId);

        // 멱등성 체크: 이미 처리된 결제인지 확인
        if (paymentRepository.existsByOrderId(
            request.orderId()
        )) {
            log.info("이미 처리된 결제입니다. DB 정보를 반환합니다. orderId={}", request.orderId());
            PaymentEntity existingPayment = paymentRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("결제 정보 불일치"));

            return PaymentResponse.from(existingPayment);
        }

        // 실제 결제 총액 검증
        if (order.totalAmount().compareTo(request.usedDepositAmount()) != 0) {
            throw new InvalidOrderAmountException();
        }

        return paymentTxService.processDepositPayment(request, userId, order);
    }

    // toss payments 사용 결제
    public Deposit validateBeforeApprove(PaymentConfirmRequest request, String userId) {
        OrderResponse order = orderFeignClient.getOrderInfo(request.orderId(), userId);

        if (order.totalAmount().compareTo(request.amount()) != 0) {
            throw new InvalidOrderAmountException();
        }

        return depositService.getDepositBalance(userId);
    }

    // DB저장 및 이벤트 처리(트랜잭션)
    @Transactional
    public PaymentResponse completeTossPayment(PaymentConfirmRequest request, String userId,
        Deposit deposit, TossApprovalResponse approve) {

        try {
            // DB 저장 및 예치금 차감
            BigDecimal usedDepositAmount = approve.depositUsedAmount();     // deposit 사용 금액
            PayType payType;

            if (deposit.getBalance().compareTo(BigDecimal.ZERO) > 0
                && usedDepositAmount.compareTo(BigDecimal.ZERO) > 0) {
                // 예치금 차감
                depositService.useDeposit(userId, usedDepositAmount);
                payType = PayType.DEPOSIT_TOSS;
            } else {
                payType = PayType.TOSS;
            }

            PaymentEntity paymentEntity = PaymentEntity.of(
                userId,
                approve.orderId(),
                approve.amount(),
                approve.depositUsedAmount(),
                approve.tossChargedAmount(),
                approve.currency(),
                payType,
                approve.paymentStatus(),
                approve.paymentKey(),
                approve.approvedAt()
            );
            PaymentEntity saved = paymentRepository.save(paymentEntity);

            // order 동기처리
            orderFeignClient.updateOrderStatus(
                request.orderId(),
                OrderStatusUpdateRequest.of(
                    OrderStatus.PAYMENT_COMPLETED, saved
                ),
                userId);

            log.info("[토스 결제 성공] orderId={}, userId={}, paymentKey={}, request={}, approve={}",
                request.orderId(), userId, request.paymentKey(), request, approve);

            return PaymentResponse.from(saved);
        } catch (Exception e) {
            log.error("[토스 결제 실패] orderId={}, userId={}, paymentKey={}, request={}",
                request.orderId(), userId, request.paymentKey(), request, e);
            throw new PaymentFailedException();
        }
    }

    // 환불 처리
    @Transactional
    public RefundResponse refundToss(PaymentEntity payment, String userId, RefundResponse refundResponse) {
        try {
            // 반드시 영속상태로 다시 조회해야 cascade 정상 작동함
            payment = paymentRepository.findById(payment.getId())
                .orElseThrow(() -> new PaymentNotFoundException());

            // 내부처리
            BigDecimal tossRefund = payment.getTossChargedAmount();

            PaymentRefundEntity refundEntity = createRefundEntity(
                payment, tossRefund, refundResponse.canceledAt()
            );

            refundEntity.refundSuccess(payment.getOrderId(), OffsetDateTime.now());
            // order 동기처리
            orderFeignClient.updateOrderStatus(
                payment.getOrderId(),
                OrderStatusUpdateRequest.of(
                    OrderStatus.REFUND_AFTER_PAYMENT, payment
                ),
                userId);
            RefundResponse.from(refundEntity);

            log.info("[토스 환불 성공] orderId={}, userId={}, paymentKey={}, refundResponse={}",
                payment.getOrderId(), userId, payment.getPaymentKey(), refundResponse);

            return refundResponse;

        } catch (Exception e) {
            log.error("[토스 환불 실패] orderId={}, userId={}, paymentKey={}, refundResponse={}",
                payment.getOrderId(), userId, payment.getPaymentKey(), refundResponse, e);
            throw e;
        }
    }

    @Transactional
    public RefundResponse refundDeposit(PaymentEntity payment, String userId) {
        try {
            // 반드시 영속상태로 다시 조회해야 cascade 정상 작동함
            payment = paymentRepository.findById(payment.getId())
                .orElseThrow(() -> new PaymentNotFoundException());

            // 내부처리
            BigDecimal depositRefund = payment.getDepositUsedAmount();

            // 예치금 환불
            depositService.refundDeposit(payment.getUsersId(), depositRefund);

            PaymentRefundEntity refundEntity = createRefundEntity(
                payment, depositRefund, OffsetDateTime.now()
            );

            refundEntity.refundSuccess(payment.getOrderId(), OffsetDateTime.now());
            // order 동기처리
            orderFeignClient.updateOrderStatus(
                payment.getOrderId(),
                OrderStatusUpdateRequest.of(
                    OrderStatus.REFUND_AFTER_PAYMENT, payment
                ),
                userId);

            RefundResponse response = RefundResponse.from(refundEntity);

            log.info("[예치금 환불 성공] orderId={}, userId={}, paymentKey={}, response={}",
                payment.getOrderId(), userId, payment.getPaymentKey(), response);

            return response;

        } catch (Exception e) {
            log.error("[예치금 환불 실패] orderId={}, userId={}, paymentKey={}",
                payment.getOrderId(), userId, payment.getPaymentKey(), e);
            throw e;
        }
    }

    @Transactional
    public RefundResponse refundTossDeposit(PaymentEntity payment, String userId, RefundResponse refundResponse) {
        try {
            // 반드시 영속상태로 다시 조회해야 cascade 정상 작동함
            payment = paymentRepository.findById(payment.getId())
                .orElseThrow(() -> new PaymentNotFoundException());

            // 내부처리
            BigDecimal depositRefund = payment.getDepositUsedAmount();
            BigDecimal tossRefund = payment.getTossChargedAmount();
            BigDecimal totalRefund = depositRefund.add(tossRefund);

            // 예치금 환불
            depositService.refundDeposit(payment.getUsersId(), depositRefund);

            PaymentRefundEntity refundEntity = createRefundEntity(
                payment, totalRefund, refundResponse.canceledAt()
            );

            refundEntity.refundSuccess(payment.getOrderId(), OffsetDateTime.now());

            // order 동기처리
            orderFeignClient.updateOrderStatus(
                payment.getOrderId(),
                OrderStatusUpdateRequest.of(
                    OrderStatus.REFUND_AFTER_PAYMENT, payment
                ),
                userId);
            RefundResponse response = RefundResponse.from(refundEntity);

            log.info("[혼합 환불 성공] orderId={}, userId={}, paymentKey={}, refundResponse={}, response={}",
                payment.getOrderId(), userId, payment.getPaymentKey(), refundResponse, response);

            return response;

        } catch (Exception e) {
            log.error("[혼합 환불 실패] orderId={}, userId={}, paymentKey={}",
                payment.getOrderId(), userId, payment.getPaymentKey(), e);
            throw e;
        }
    }

    // 환불 공통 메서드
    private PaymentRefundEntity createRefundEntity(PaymentEntity payment, BigDecimal refundAmount,
        OffsetDateTime canceledAt) {
        PaymentRefundEntity refund = PaymentRefundEntity.of(
            payment.getPaymentKey(),
            payment.getOrderId(),
            refundAmount,
            "사용자 요청 환불",
            payment.getStatus(),
            payment.getApprovedAt(),
            canceledAt
        );
        payment.linkRefund(refund);
        return refund;
    }

    public PaymentResponse findByOrderId(String orderId) {
        PaymentEntity paymentEntity = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문 번호입니다: " + orderId));
        return PaymentResponse.from(paymentEntity);
    }
}