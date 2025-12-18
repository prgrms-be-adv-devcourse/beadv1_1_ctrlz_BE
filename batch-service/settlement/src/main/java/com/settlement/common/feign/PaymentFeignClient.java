package com.settlement.common.feign;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.settlement.common.dto.BaseResponse;
import com.settlement.dto.PaymentResponse;

@FeignClient(name = "payment-service", url = "${custom.feign.url.payment-service}")
public interface PaymentFeignClient {

    @GetMapping("/api/payments/settlement")
    BaseResponse<List<PaymentResponse>> getPaymentsForSettlement(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate);
}
