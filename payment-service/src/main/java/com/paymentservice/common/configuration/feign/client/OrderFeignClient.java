package com.paymentservice.common.configuration.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.paymentservice.common.configuration.feign.configuration.UserClientConfiguration;
import com.paymentservice.common.configuration.feign.dto.OrderStatusUpdateRequest;
import com.paymentservice.common.model.order.OrderResponse;

@FeignClient(
    name = "order-service",
    url = "${custom.feign.url.order-service}",
    configuration = {UserClientConfiguration.class}
)
public interface OrderFeignClient {

    @GetMapping("/api/orders/{orderId}/{userId}")
    OrderResponse getOrderInfo(@PathVariable("orderId") String orderId,
        @PathVariable("userId") String userId
    );

    @PatchMapping("/api/orders/{orderId}/status/{userId}")
    void updateOrderStatus(
        @PathVariable("orderId") String orderId,
        @RequestBody OrderStatusUpdateRequest request,
        @PathVariable("userId") String userId
    );


}
