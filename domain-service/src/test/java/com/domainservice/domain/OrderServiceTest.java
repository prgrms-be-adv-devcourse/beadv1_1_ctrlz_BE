package com.domainservice.domain;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.common.exception.CustomException;
import com.domainservice.domain.cart.model.entity.CartItem;
import com.domainservice.domain.cart.repository.CartItemJpaRepository;
import com.domainservice.domain.order.model.dto.OrderResponse;
import com.domainservice.domain.order.model.entity.Order;
import com.domainservice.domain.order.model.entity.OrderStatus;
import com.domainservice.domain.order.repository.OrderJpaRepository;
import com.domainservice.domain.order.service.OrderService;
import com.domainservice.domain.post.post.model.dto.response.ProductPostResponse;
import com.domainservice.domain.post.post.service.ProductPostService;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private OrderJpaRepository orderJpaRepository;

	@Mock
	private CartItemJpaRepository cartItemJpaRepository;

	@Mock
	private ProductPostService productPostService;

	@InjectMocks
	private OrderService orderService;

	private String userId;
	private CartItem cartItem;
	private ProductPostResponse productResponse;
	private Order order;

	@BeforeEach
	void setUp() {
		userId = "user123";

		cartItem = CartItem.builder()
			.productPostId("product1")
			.build();

		productResponse = new ProductPostResponse(
			"product1",
			"seller1",
			"category1",
			"상품A",
			"상품A 상세명",
			10000,
			"테스트 상품",
			null,
			null,
			List.of(),
			null,
			List.of(),
			LocalDateTime.now(),
			LocalDateTime.now()
		);

		order = Order.builder()
			.buyerId(userId)
			.orderName("상품A")
			.orderStatus(OrderStatus.PAYMENT_PENDING)
			.orderItems(new ArrayList<>())
			.build();
	}

	@Test
	@DisplayName("주문 생성 - 정상 케이스")
	void test1() {
		// given
		when(cartItemJpaRepository.findAllByIdIn(anyList())).thenReturn(List.of(cartItem));
		when(productPostService.getProductPostById("product1")).thenReturn(productResponse);
		when(productPostService.isSellingTradeStatus("product1")).thenReturn(true);
		when(orderJpaRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

		// when
		OrderResponse result = orderService.createOrder(userId, List.of("cartItem1"));

		// then
		assertThat(result).isNotNull();
		assertThat(result.buyerId()).isEqualTo(userId);
		assertThat(result.orderStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
		assertThat(result.orderItems()).hasSize(1);

		verify(orderJpaRepository).save(any(Order.class));
		verify(productPostService, times(1)).getProductPostById("product1");
	}

	@Test
	@DisplayName("주문 생성 실패 - 상품 판매중 아님")
	void test2() {
		// given
		when(cartItemJpaRepository.findAllByIdIn(anyList())).thenReturn(List.of(cartItem));
		when(productPostService.getProductPostById("product1")).thenReturn(productResponse);
		when(productPostService.isSellingTradeStatus("product1")).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> orderService.createOrder(userId, List.of("cartItem1")))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("상품"); // OrderExceptionCode.PRODUCT_NOT_AVAILABLE.getMessage() 내용 일부

		verify(orderJpaRepository, never()).save(any());
	}

	@Test
	@DisplayName("주문 취소 - 결제대기 상태에서 취소 가능")
	void test3() {
		// given
		order.setOrderStatus(OrderStatus.PAYMENT_PENDING);
		when(orderJpaRepository.findById("order1")).thenReturn(Optional.of(order));
		when(orderJpaRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

		// when
		OrderResponse result = orderService.cancelOrder("order1", userId);

		// then
		assertThat(result.orderStatus()).isEqualTo(OrderStatus.CANCELLED);
		verify(orderJpaRepository).save(order);
	}

	@Test
	@DisplayName("주문 취소 실패 - 다른 사용자 요청")
	void test4() {
		// given
		when(orderJpaRepository.findById("order1")).thenReturn(Optional.of(order));

		// when & then
		assertThatThrownBy(() -> orderService.cancelOrder("order1", "otherUser"))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("권한");
	}

	@Test
	@DisplayName("주문 확정 - 결제완료 상태에서 구매확정 성공")
	void test5() {
		// given
		order.setOrderStatus(OrderStatus.PAYMENT_COMPLETED);
		when(orderJpaRepository.findById("order1")).thenReturn(Optional.of(order));
		when(orderJpaRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

		// when
		OrderResponse result = orderService.confirmPurchase("order1", userId);

		// then
		assertThat(result.orderStatus()).isEqualTo(OrderStatus.PURCHASE_CONFIRMED);
		verify(orderJpaRepository).save(order);
	}

	@Test
	@DisplayName("주문 확정 실패 - 결제완료 상태가 아닐 경우")
	void test6() {
		// given
		order.setOrderStatus(OrderStatus.PAYMENT_PENDING);
		when(orderJpaRepository.findById("order1")).thenReturn(Optional.of(order));

		// when & then
		assertThatThrownBy(() -> orderService.confirmPurchase("order1", userId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining("구매확정");
	}
}
