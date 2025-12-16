package com.domainservice.domain.order.api;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.common.model.web.PageResponse;
import com.domainservice.domain.order.docs.CancelOrderApiDocs;
import com.domainservice.domain.order.docs.CancelOrderItemApiDocs;
import com.domainservice.domain.order.docs.ConfirmPurchaseApiDocs;
import com.domainservice.domain.order.docs.CreateOrderApiDocs;
import com.domainservice.domain.order.docs.GetOrderApiDocs;
import com.domainservice.domain.order.docs.GetOrderInfoApiDocs;
import com.domainservice.domain.order.docs.GetOrderListApiDocs;
import com.domainservice.domain.order.docs.GetOrdersForSettlementApiDocs;
import com.domainservice.domain.order.docs.UpdateOrderStatusApiDocs;
import com.domainservice.domain.order.model.dto.CreateOrderRequest;
import com.domainservice.domain.order.model.dto.OrderResponse;
import com.domainservice.domain.order.model.dto.OrderStatusUpdateRequest;
import com.domainservice.domain.order.service.OrderService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Order", description = "주문 API")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @CreateOrderApiDocs
    @PostMapping
    public BaseResponse<OrderResponse> createOrder(
        @RequestHeader(value = "X-REQUEST-ID") String userId,
        @RequestBody CreateOrderRequest orderRequest) {

        return new BaseResponse<>(
            orderService.createOrder(userId, orderRequest.cartItemIds()),
            "주문 생성 성공했습니다");
    }

    @CancelOrderApiDocs
    @PatchMapping("/{orderId}/cancel")
    public BaseResponse<OrderResponse> cancelOrder(
        @RequestHeader(value = "X-REQUEST-ID") String userId,
        @PathVariable String orderId) {
        return new BaseResponse<>(
            orderService.cancelOrder(orderId, userId), "주문 취소 성공했습니다");
    }

    @ConfirmPurchaseApiDocs
    @PatchMapping("/{orderId}/confirm")
    public BaseResponse<OrderResponse> confirmPurchase(
        @RequestHeader(value = "X-REQUEST-ID") String userId,
        @PathVariable String orderId) {
        return new BaseResponse<>(orderService.confirmPurchase(orderId, userId),
            "주문 확정 성공했습니다");
    }

    @CancelOrderItemApiDocs
    @PatchMapping("/{orderId}/items/{orderItemId}/cancel")
    public BaseResponse<OrderResponse> cancelOrderItem(
        @RequestHeader(value = "X-REQUEST-ID") String userId,
        @PathVariable String orderId,
        @PathVariable String orderItemId) {
        return new BaseResponse<>(
            orderService.cancelOrderItem(orderId, userId, orderItemId),
            "주문 일부 취소 성공했습니다");
    }

    @GetOrderInfoApiDocs
    @GetMapping("/{orderId}/{userId}")
    public OrderResponse getOrderInfo(
        @PathVariable("orderId") String orderId,
        @PathVariable("userId") String userId
    ) {
        return orderService.getOrderById(orderId, userId);
    }

    @GetOrderApiDocs
    @GetMapping("/{orderId}")
    public BaseResponse<OrderResponse> getOrder(
        @PathVariable String orderId,
        @RequestHeader(value = "X-REQUEST-ID") String userId) {
        OrderResponse orderById = orderService.getOrderById(orderId, userId);
        return new BaseResponse<>(
            orderById,
            "주문 상세 조회 성공했습니다");

    }

    @GetOrderListApiDocs
    @GetMapping
    public PageResponse<List<OrderResponse>> getOrderList(
        @RequestHeader(value = "X-REQUEST-ID") String userId,
        @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<List<OrderResponse>> orderListByUserId = orderService.getOrderListByUserId(userId, pageable);

        return orderListByUserId;
    }

    @UpdateOrderStatusApiDocs
    @PatchMapping("/{orderId}/status/{userId}")
    public void updateOrderStatus(
        @PathVariable String orderId,
        @RequestBody OrderStatusUpdateRequest request,
        @PathVariable String userId
    ) {
        orderService.updateStatus(
            orderId,
            request.orderStatus(),
            request.paymentId()
        );

    }

    @GetOrdersForSettlementApiDocs
    @GetMapping("/settlement")
    public BaseResponse<List<OrderResponse>> getOrdersForSettlement(
        @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return new BaseResponse<>(
            orderService.getOrdersForSettlement(startDate, endDate),
            "정산용 주문 목록 조회 성공했습니다");

    }
}