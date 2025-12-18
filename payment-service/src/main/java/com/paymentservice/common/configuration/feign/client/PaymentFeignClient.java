package com.paymentservice.common.configuration.feign.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.paymentservice.deposit.model.dto.TossApproveDepositRequest;
import com.paymentservice.payment.model.dto.RefundResponse;
import com.paymentservice.payment.model.dto.TossApproveRequest;
import com.paymentservice.payment.model.dto.TossCancelRequest;

@FeignClient(name = "tossPaymentClient", url = "${custom.payment.toss.targetUrl}")
public interface PaymentFeignClient {

    @PostMapping(value = "/confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> requestPayment(
        @RequestBody TossApproveRequest request,
        @RequestHeader("Authorization") String authHeader
    );

    // cancel요청 시 body에 cancelAmount, cancelReason필요
    @PostMapping(value = "/{paymentKey}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    RefundResponse refundPayment(
        @PathVariable("paymentKey") String paymentKey,
        @RequestBody TossCancelRequest request,
        @RequestHeader("Authorization") String authHeader
    );

    @PostMapping(value = "/confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> requestDeposit(
        @RequestBody TossApproveDepositRequest request,
        @RequestHeader("Authorization") String authHeader
    );
}
