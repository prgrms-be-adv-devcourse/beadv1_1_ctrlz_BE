package com.settlement.common.feign;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.common.model.web.BaseResponse;
import com.settlement.dto.OrderResponse;

@FeignClient(name = "order-service", url = "${custom.feign.url.order-service}"
// configuration = {UserClientConfiguration.class} // 필요시 추가
)
public interface OrderFeignClient {

    @GetMapping("/api/orders/{orderId}")
    OrderResponse getOrder(@PathVariable String orderId,
            @RequestHeader(value = "X-REQUEST-ID") String userId);

    @GetMapping("/api/orders/settlement")
    BaseResponse<List<OrderResponse>> getOrdersForSettlement(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate);
}
