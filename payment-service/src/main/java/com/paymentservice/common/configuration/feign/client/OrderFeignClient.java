package com.paymentservice.common.configuration.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.paymentservice.common.configuration.feign.configuration.UserClientConfiguration;
import com.paymentservice.common.model.order.OrderResponse;

@FeignClient(
    name = "order-service",
    url = "${custom.feign.url.order-service}",
    configuration = {UserClientConfiguration.class}
)
public interface OrderFeignClient {

    @GetMapping("/api/orders/{orderId}")
    OrderResponse getOrder(@PathVariable String orderId,
        @RequestHeader(value = "X-REQUEST-ID") String userId
    );
}
