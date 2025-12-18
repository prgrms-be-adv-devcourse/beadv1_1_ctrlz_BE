package com.paymentservice.payment.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paymentservice.common.configuration.feign.client.OrderFeignClient;
import com.paymentservice.common.configuration.feign.dto.OrderStatusUpdateRequest;
import com.paymentservice.common.model.order.OrderResponse;
import com.paymentservice.common.model.order.OrderStatus;
import com.paymentservice.deposit.service.DepositService;
import com.paymentservice.payment.exception.PaymentFailedException;
import com.paymentservice.payment.model.dto.PaymentConfirmRequest;
import com.paymentservice.payment.model.dto.PaymentResponse;
import com.paymentservice.payment.model.entity.PaymentEntity;
import com.paymentservice.payment.model.enums.PayType;
import com.paymentservice.payment.model.enums.PaymentStatus;
import com.paymentservice.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentTxService {

    private final PaymentRepository paymentRepository;
    private final DepositService depositService;
    private final OrderFeignClient orderFeignClient;
    private static final Logger log = LoggerFactory.getLogger("API." + PaymentService.class.getName());

    // 기존 PaymentService에 있던 메서드로, 같은 클래스 내부에서 this.processDepositPayment()형태로 호출되면 @Transactional이 아예 동작하지 않는다.
    // 스프링 AOP는 프록시 기반이라 외부 호출만 트랜잭션 적용됨
    // 따라서, 예치금이 차감만 되고 결제 실패 상태가 발생
    // => 트랜잭션 메서드를 다른 서비스로 분리함
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
                "paymentKey",
                OffsetDateTime.now()
            );
            paymentRepository.save(paymentEntity);

            // order 동기처리
            orderFeignClient.updateOrderStatus(
                request.orderId(),
                OrderStatusUpdateRequest.of(
                    OrderStatus.PAYMENT_COMPLETED, paymentEntity
                ),
                userId);

            log.info("[예치금 결제 성공] orderId={}, userId={}, request={}",
                request.orderId(), userId, request);

            return PaymentResponse.from(paymentEntity);
        } catch (Exception e) {
            log.error("[예치금 결제 실패] orderId={}, userId={}, request={}",
                request.orderId(), userId, request, e);
            throw new PaymentFailedException();
        }
    }



}
