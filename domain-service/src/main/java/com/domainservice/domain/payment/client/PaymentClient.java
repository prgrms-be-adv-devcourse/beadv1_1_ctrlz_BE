package com.domainservice.domain.payment.client;

import java.util.Map;

import org.springframework.cloud.openfeign .FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.domainservice.domain.payment.model.dto.PaymentConfirmRequest;
import com.domainservice.domain.payment.model.dto.PaymentReadyResponse;


@FeignClient(name = "tossPaymentClient", url = "https://api.tosspayments.com/v1/payments/confirm")
public interface PaymentClient {

    @PostMapping
    Map<String, Object> requestPayment(
        @RequestBody Map<String, Object> request,
        @RequestHeader("Authorization") String authHeader,
        @RequestHeader("Content-Type") String contentType
    );





    // @Value("${payment.toss.test_secret_api_key}")
    // private String secretApiKey;

    // @Value("${payment.toss.targetUrl}")
    // private String targetUrl;
    //
    // @Value("${payment.toss.success_url}")
    // private String successUrl;
    //
    // @Value("${payment.toss.fail_url}")
    // private String failUrl;
    //
    // private final RestTemplate restTemplate = new RestTemplate();
    //
    // /** 결제 승인(confirm */
    // public Map<String, Object> requestPayment(PaymentRequest request) {
    //     String authHeader =
    //         "Basic "
    //             + Base64.getEncoder()
    //             .encodeToString((secretApiKey + ":").getBytes(StandardCharsets.UTF_8));
    //
    //     HttpHeaders headers = new HttpHeaders();
    //     headers.setContentType(MediaType.APPLICATION_JSON);
    //     headers.set("Authorization", authHeader);
    //
    //     HttpEntity<PaymentRequest> entity = new HttpEntity<>(request, headers);
    //
    //     ResponseEntity<Map> response =
    //         restTemplate.exchange(targetUrl, HttpMethod.POST, entity, Map.class);
    //
    //     return response.getBody();
    // }
    //
    // public String getSuccessUrl() {
    //     return successUrl;
    // }
    //
    // public String getFailUrl() {
    //     return failUrl;
    // }
}
