package com.paymentservice.payment.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.exception.CustomException;
import com.common.exception.vo.OrderExceptionCode;
import com.paymentservice.common.configuration.feign.client.OrderFeignClient;
import com.paymentservice.common.model.order.OrderResponse;
import com.paymentservice.deposit.model.entity.Deposit;
import com.paymentservice.deposit.service.DepositService;
import com.paymentservice.payment.client.PaymentTossClient;
import com.paymentservice.payment.exception.InvalidOrderAmountException;
import com.paymentservice.payment.exception.PaymentFailedException;
import com.paymentservice.payment.model.dto.PaymentConfirmRequest;
import com.paymentservice.payment.model.dto.PaymentReadyResponse;
import com.paymentservice.payment.model.dto.PaymentResponse;
import com.paymentservice.payment.model.dto.RefundResponse;
import com.paymentservice.payment.model.dto.TossApprovalResponse;
import com.paymentservice.payment.model.entity.PaymentEntity;
import com.paymentservice.payment.model.entity.PaymentRefundEntity;
import com.paymentservice.payment.model.enums.PayType;
import com.paymentservice.payment.model.enums.PaymentStatus;
import com.paymentservice.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentLogService paymentLogService;
    private final OrderFeignClient orderFeignClient;
    private final DepositService depositService;
    private final PaymentTossClient paymentTossClient;

    // 결제 정보
    public PaymentReadyResponse getPaymentReadyInfo(String orderId, String userId) {
        OrderResponse order = orderFeignClient.getOrder(orderId, userId);
        // order의 buyerId와 로그인 된 userId가 같은지 검증
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
    // order, 금액 검증
    public PaymentResponse depositPayment(PaymentConfirmRequest request, String userId) {
        OrderResponse order = orderFeignClient.getOrder(request.orderId(), userId);

        // 실제 결제 총액 검증
        if (order.totalAmount().compareTo(request.usedDepositAmount()) != 0) {
            throw new InvalidOrderAmountException();
        }
        paymentLogService.logRequest(
            userId, request.orderId(), request.paymentKey(), request
        );

        return processDepositPayment(request, userId, order);
    }

    // 예치금 사용 & order 상태, DB 저장(트랜잭션)
    @Transactional
    public PaymentResponse processDepositPayment(PaymentConfirmRequest request, String userId, OrderResponse order) {

        try {
            // 예치금 차감
            depositService.useDeposit(userId, request.usedDepositAmount());

            PaymentEntity paymentEntity = PaymentEntity.of(
                userId,
                order.orderId(),
                order.totalAmount(),
                request.amount(),
                BigDecimal.ZERO,
                "KRW",
                PayType.DEPOSIT,
                PaymentStatus.SUCCESS,
                null,
                OffsetDateTime.now()
            );
            paymentRepository.save(paymentEntity);

            // TODO: 비동기 주문 상태 변경 이벤트 발행

            paymentLogService.logSuccess(
                request.orderId(), userId, null, request, paymentEntity
            );

            return PaymentResponse.from(paymentEntity);
        } catch (Exception e) {
            paymentLogService.logFail(
                request.orderId(), userId, null, e.getMessage(), request
            );
            throw new PaymentFailedException();
        }
    }

    // toss payments 사용 결제
    // order, deposit 조회 및 validation
    public PaymentResponse tossPayment(PaymentConfirmRequest request, String userId) {
        OrderResponse order = orderFeignClient.getOrder(request.orderId(), userId);
        Deposit deposit = depositService.getDepositBalance(userId);

        // 실제 결제 총액 검증
        if (order.totalAmount().compareTo(request.amount()) != 0) {
            throw new InvalidOrderAmountException();
        }

        paymentLogService.logRequest(
            userId, request.orderId(), request.paymentKey(), request
        );

        return processTossPayment(request, userId, deposit);
    }

    // DB저장 및 이벤트 처리(트랜잭션)
    @Transactional
    public PaymentEntity completePayment(TossApprovalResponse approve, String userId, Deposit deposit) {

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

        // TODO: 비동기 주문 상태 변경 이벤트 발행

        return paymentRepository.save(paymentEntity);
    }

    public PaymentResponse processTossPayment(PaymentConfirmRequest request, String userId,
        Deposit deposit) {

        try {
            // toss payments api
            TossApprovalResponse approve = paymentTossClient.approve(request);

            // DB 저장 및 예치금 차감
            PaymentEntity paymentEntity = completePayment(approve, userId, deposit);

            paymentLogService.logSuccess(
                request.orderId(), userId, request.paymentKey(), request, approve
            );

            return PaymentResponse.from(paymentEntity);
        } catch (Exception e) {
            paymentLogService.logFail(
                request.orderId(), userId, request.paymentKey(), e.getMessage(), request
            );
            throw new PaymentFailedException();
        }
    }

    // 환불 처리
    public RefundResponse refundOrder(PaymentEntity payment, boolean includeDeposit, String userId) {
        paymentLogService.logRequest(
            userId,
            payment.getOrderId(),
            payment.getPaymentKey(),
            null
        );

        RefundResponse approvalResponse = null;
        RefundResponse response = null;
        try {
            // toss api
            approvalResponse = paymentTossClient.refund(payment);

            // 내부처리
            response = processRefundOrder(payment, approvalResponse, includeDeposit);
            paymentLogService.logSuccess(
                payment.getOrderId(),
                userId,
                payment.getPaymentKey(),
                approvalResponse,
                response
            );
            return response;

        } catch (Exception e) {
            paymentLogService.logFail(
                payment.getOrderId(),
                userId,
                payment.getPaymentKey(),
                e.getMessage(),
                response
            );
            throw e;
        }

    }

    @Transactional
    public RefundResponse processRefundOrder(PaymentEntity payment, RefundResponse response, boolean includeDeposit) {

        BigDecimal depositRefund = payment.getDepositUsedAmount();
        BigDecimal tossRefund = payment.getTossChargedAmount();

        PaymentRefundEntity refundEntity = PaymentRefundEntity.of(
            payment.getPaymentKey(),
            payment.getOrderId(),
            depositRefund.add(tossRefund),
            "사용자 요청 환불",
            payment.getStatus(),
            payment.getApprovedAt(),
            response.canceledAt()
        );
        payment.linkRefund(refundEntity);

        // 예치금 환불
        if (includeDeposit && depositRefund.compareTo(BigDecimal.ZERO) > 0) {
            depositService.refundDeposit(payment.getUsersId(), depositRefund);
        }

        refundEntity.refundSuccess(payment.getOrderId(), OffsetDateTime.now());

        //TODO: 주문상태 이벤트

        return RefundResponse.from(refundEntity);
    }
}