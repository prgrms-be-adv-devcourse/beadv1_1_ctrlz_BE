package com.settlement.common.feign;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.common.model.web.BaseResponse;
import com.settlement.dto.PaymentResponse;

@FeignClient(name = "payment-service", url = "${custom.feign.url.payment-service}")
public interface PaymentFeignClient {

    @GetMapping("/api/payments/settlement")
    BaseResponse<List<PaymentResponse>> getPaymentsForSettlement(
            @RequestParam("startDate") LocalDateTime startDate,
            @RequestParam("endDate") LocalDateTime endDate);
}
