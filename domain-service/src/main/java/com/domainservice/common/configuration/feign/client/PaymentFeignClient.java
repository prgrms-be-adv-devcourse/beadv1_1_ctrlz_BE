package com.domainservice.common.configuration.feign.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "tossPaymentClient", url = "https://api.tosspayments.com/v1/payments")
public interface PaymentFeignClient {

    @PostMapping(value = "/confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> requestPayment(
        @RequestBody Map<String, Object> request,
        @RequestHeader("Authorization") String authHeader
    );

    // cancel요청 시 body에 cancelAmount, cancelReason필요
    @PostMapping(value = "/{paymentKey}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> refundPayment(
        @PathVariable("paymentKey") String paymentKey,
        @RequestBody Map<String, Object> request,
        @RequestHeader("Authorization") String authHeader
    );
}
