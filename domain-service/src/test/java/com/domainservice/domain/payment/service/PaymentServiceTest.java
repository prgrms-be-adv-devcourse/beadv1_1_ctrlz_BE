package com.domainservice.domain.payment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.common.exception.CustomException;
import com.common.exception.vo.DepositExceptionCode;
import com.common.exception.vo.PaymentExceptionCode;
import com.domainservice.domain.deposit.model.dto.DepositResponse;
import com.domainservice.domain.deposit.model.entity.Deposit;
import com.domainservice.domain.deposit.service.DepositService;
import com.domainservice.domain.order.model.entity.Order;
import com.domainservice.domain.order.model.entity.OrderItem;
import com.domainservice.domain.order.model.entity.OrderStatus;
import com.domainservice.domain.order.repository.OrderRepository;
import com.domainservice.domain.payment.model.dto.PaymentConfirmRequest;
import com.domainservice.domain.payment.model.dto.PaymentResponse;
import com.domainservice.domain.payment.model.dto.RefundResponse;
import com.domainservice.domain.payment.model.entity.PaymentEntity;
import com.domainservice.domain.payment.model.enums.PaymentStatus;
import com.domainservice.domain.payment.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private DepositService depositService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentLogService paymentLogService;

    @InjectMocks
    private PaymentService paymentService;

    private Order order;
    private OrderItem orderItem;
    private Deposit deposit;

    @BeforeEach
    void setUp() throws Exception {
        // 기본 주문 세팅
        order = Order.builder()
            .buyerId("user123")
            .orderName("아이폰17")
            .orderStatus(OrderStatus.PAYMENT_PENDING)
            .build();

        Field orderIdField = Order.class.getSuperclass().getDeclaredField("id");
        orderIdField.setAccessible(true);
        orderIdField.set(order, "order123");

        orderItem = OrderItem.builder()
            .priceSnapshot(new BigDecimal(2000))
            .build();

        Field itemIdField = OrderItem.class.getSuperclass().getDeclaredField("id");
        itemIdField.setAccessible(true);
        itemIdField.set(orderItem, "item123");

        orderItem.setOrder(order);
        order.addOrderItem(orderItem);

        deposit = Deposit.builder()
            .userId("user123")
            .balance(new BigDecimal(10000))
            .build();
    }

    @Test
    @DisplayName("예치금만 사용한 결제 처리")
    void test1() {
        // given
        when(orderRepository.findById("order123")).thenReturn(Optional.of(order));

        PaymentConfirmRequest request = new PaymentConfirmRequest(
            null, "order123", new BigDecimal(2000), new BigDecimal(2000), BigDecimal.ZERO
        );

        // when
        PaymentResponse response = paymentService.depositPayment(request);

        // then
        assertThat(response.status()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(response.depositUsedAmount()).isEqualTo(new BigDecimal(2000));
        assertThat(response.tossChargedAmount()).isEqualTo(BigDecimal.ZERO);

        verify(paymentRepository).save(any(PaymentEntity.class));

        verify(depositService).useDeposit(eq(deposit.getUserId()), eq(new BigDecimal(2000)));

        verify(paymentLogService, times(2)).saveLog(
            eq("order123"),
            eq(deposit.getUserId()),
            any(),
            anyString(),    // "REQUEST"/"SUCCESS" 등
            anyString(),    // requestJson
            any(),          // responseJson
            any()           // errorMessage
        );
    }

    @Test
    @DisplayName("환불 처리 시 예치금 환불 및 로그 기록")
    void test2() {
        // given
        PaymentEntity payment = PaymentEntity.builder()
            .paymentKey("paymentKey123")
            .order(order)
            .usersId("user123")
            .tossChargedAmount(BigDecimal.ZERO) // Toss 결제 없이 예치금만 사용
            .depositUsedAmount(new BigDecimal("3000"))
            .approvedAt(OffsetDateTime.now())
            .status(PaymentStatus.SUCCESS)
            .build();

        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(payment);
        when(orderRepository.findById("order123")).thenReturn(Optional.of(order));

        DepositResponse depositResponse = new DepositResponse("depositId123", new BigDecimal("10000"), "예치금이 환불되었습니다.");
        when(depositService.refundDeposit("user123", new BigDecimal("3000"))).thenReturn(depositResponse);

        // paymentLogService는 void이므로 doNothing() 사용 가능
        doNothing().when(paymentLogService).saveLog(
            anyString(), anyString(), anyString(), anyString(), any(), any(), any()
        );

        // when
        RefundResponse response = paymentService.refundOrder(payment, true);

        // then
        assertThat(response).isNotNull();
        assertThat(response.paymentKey()).isEqualTo("paymentKey123");

        // 예치금 환불 호출
        verify(depositService).refundDeposit("user123", new BigDecimal("3000"));

        // REFUND_REQUEST 로그 호출
        verify(paymentLogService).saveLog(
            eq("order123"),
            eq("user123"),
            eq("paymentKey123"),
            eq("REFUND_REQUEST"),
            any(),
            any(),
            isNull()
        );

        // REFUND_SUCCESS 로그 호출
        verify(paymentLogService).saveLog(
            eq("order123"),
            eq("user123"),
            eq("paymentKey123"),
            eq("REFUND_SUCCESS"),
            any(),
            any(),
            isNull()
        );

        // 호출 확인
        verify(paymentRepository).save(payment);

        // 주문 상태 변경 확인
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.REFUND_AFTER_PAYMENT);
    }

    @Test
    @DisplayName("예치금 결제 실패 시 처리 및 FAIL 로그 기록")
    void test3() {
        // given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
            null, "order123", new BigDecimal(5000), new BigDecimal(0), new BigDecimal(5000)
        );

        when(orderRepository.findById("order123")).thenReturn(Optional.of(order));

        // 예치금 사용 시 예외 발생
        doThrow(new CustomException(DepositExceptionCode.INSUFFICIENT_BALANCE.getMessage()))
            .when(depositService).useDeposit(eq(order.getBuyerId()), eq(new BigDecimal(5000)));

        // when & then
        assertThatThrownBy(() -> paymentService.depositPayment(request))
            .isInstanceOf(CustomException.class)
            .hasMessage(PaymentExceptionCode.PAYMENT_FAILED.getMessage());

        // FAIL 로그 기록
        verify(paymentLogService).saveLog(
            eq("order123"),
            eq(order.getBuyerId()),
            any(),
            eq("FAIL"),
            any(),
            any(),
            anyString()
        );
    }

}
