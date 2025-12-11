package com.settlement.common.configuration.feign.client;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.common.model.web.BaseResponse;
import com.settlement.common.model.payment.PaymentResponse;

@FeignClient(name = "payment-service", url = "${custom.feign.url.payment-service}")
public interface PaymentFeignClient {

    @GetMapping("/api/payments/settlement")
    BaseResponse<List<PaymentResponse>> getPaymentsForSettlement(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate);
}
