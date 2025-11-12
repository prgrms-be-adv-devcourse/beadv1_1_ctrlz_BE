package com.domainservice.domain.payment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.common.exception.CustomException;
import com.common.exception.vo.PaymentExceptionCode;
import com.domainservice.domain.deposit.model.entity.Deposit;
import com.domainservice.domain.deposit.service.DepositService;
import com.domainservice.domain.order.model.entity.Order;
import com.domainservice.domain.order.model.entity.OrderItem;
import com.domainservice.domain.order.model.entity.OrderStatus;
import com.domainservice.domain.order.repository.OrderRepository;
import com.domainservice.domain.payment.client.PaymentClient;
import com.domainservice.domain.payment.model.dto.PaymentConfirmRequest;
import com.domainservice.domain.payment.model.dto.PaymentResponse;
import com.domainservice.domain.payment.model.entity.PaymentEntity;
import com.domainservice.domain.payment.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private DepositService depositService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Order order;
    private OrderItem orderItem;
    private Deposit deposit;

    @BeforeEach
    void setUp() throws Exception {
        // 기본 주문 세팅
        order = Order.builder()
            .buyerId("order123")
            .orderName("아이폰17")
            .orderStatus(OrderStatus.PAYMENT_PENDING)
            .build();

        Field orderIdField = Order.class.getSuperclass().getDeclaredField("id");
        orderIdField.setAccessible(true);
        orderIdField.set(order, "order123");

        orderItem = OrderItem.builder()
            .quantity(1)
            .priceSnapshot(2000)
            .build();

        Field itemIdField = OrderItem.class.getSuperclass().getDeclaredField("id");
        itemIdField.setAccessible(true);
        itemIdField.set(orderItem, "item123");

        orderItem.setOrder(order);
        order.addOrderItem(orderItem);

        deposit = Deposit.builder()
            .userId("user123")
            .balance(10000)
            .build();
    }

    @Test
    @DisplayName("예치금만 사용한 결제 처리")
    void test1() {
        // given
        order.getOrderItems().clear();
        OrderItem item = OrderItem.builder()
            .quantity(1)
            .priceSnapshot(500)
            .build();

        item.setOrder(order);
        order.addOrderItem(item);

        when(orderRepository.findById("order123")).thenReturn(Optional.of(order));
        when(depositService.getDepositByUserId("user123")).thenReturn(deposit);

        PaymentConfirmRequest request = new PaymentConfirmRequest(
            "user123", null, "order123", 500, true
        );

        // when
        PaymentResponse response = paymentService.processPayment(request);

        // then
        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.depositUsedAmount()).isEqualTo(500);
        assertThat(response.tossChargedAmount()).isEqualTo(0);
        verify(paymentRepository).save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("예치금 + 토스 결제 혼합 처리")
    void test2() {
        // given
        order.getOrderItems().clear();
        OrderItem item = OrderItem.builder()
            .quantity(1)
            .priceSnapshot(10500)
            .build();

        item.setOrder(order);
        order.addOrderItem(item);

        when(orderRepository.findById("order123")).thenReturn(Optional.of(order));
        when(depositService.getDepositByUserId("user123")).thenReturn(deposit);

        // Toss 결제 클라이언트 더미 응답
        Map<String, Object> tossResponse = new HashMap<>();
        tossResponse.put("currency", "KRW");
        tossResponse.put("status", "SUCCESS");
        tossResponse.put("paymentKey", "pay_dummy123");
        tossResponse.put("failureReason", null);
        tossResponse.put("approvedAt", LocalDateTime.now().toString());
        when(paymentClient.requestPayment(any(Map.class), anyString(), eq("application/json"))).thenReturn(tossResponse);

        PaymentConfirmRequest request = new PaymentConfirmRequest(
            "user123", "dummyPaymentKey", "order123", 10500, true
        );

        // when
        PaymentResponse response = paymentService.processPayment(request);

        // then
        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.depositUsedAmount()).isEqualTo(10000);
        assertThat(response.tossChargedAmount()).isEqualTo(500);
        assertThat(response.paymentKey()).isEqualTo("pay_dummy123");
        verify(paymentClient).requestPayment(any(Map.class), anyString(), eq("application/json"));
        verify(paymentRepository).save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("주문 존재하지 않을 시 예외")
    void test3() {
        // given
        when(orderRepository.findById("order999")).thenReturn(Optional.empty());

        PaymentConfirmRequest request = new PaymentConfirmRequest(
            "user123", "dummyPaymentKey", "order999", 1500, true
        );

        // when & then
        assertThatThrownBy(() -> paymentService.processPayment(request))
            .isInstanceOf(CustomException.class)
            .hasMessage(PaymentExceptionCode.ORDER_NOT_FOUND.getMessage());
    }

}
