package com.domainservice.domain.payment.api;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.domainservice.domain.payment.model.dto.PaymentConfirmRequest;
import com.domainservice.domain.payment.model.dto.PaymentReadyResponse;
import com.domainservice.domain.payment.model.dto.PaymentResponse;
import com.domainservice.domain.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentApiController {

    private final PaymentService paymentService;

    /** 결제 승인 요청 처리 */
    @PostMapping("/confirm")
    public String confirmPayment(@RequestBody PaymentConfirmRequest request) {
        try {
            PaymentResponse response = paymentService.processPayment(request);

            if (response.isSuccess()) {
                log.info("결제 성공: {}", request.orderId());
                return "redirect:/payments/success?orderId=" + request.orderId();
            } else {
                log.info("결제 실패: {}", request.orderId());
                return "redirect:/payments/fail?orderId=" + request.orderId();
            }
        } catch (Exception e) {
            log.error("결제 처리 중 오류 발생", e);
            return "redirect:/payments/fail?orderId=" + request.orderId();
        }
    }

    /** 결제 준비 정보 조회 */
    @GetMapping("/ready/{orderId}")
    public BaseResponse<PaymentReadyResponse> getPaymentReadyInfo(@PathVariable String orderId) {
        return new BaseResponse<>(paymentService.getPaymentReadyInfo(orderId), "결제 요청이 정상적으로 처리되었습니다.");
    }

    /** 실제 결제 요청(결제 승인 단계) */
    // @PostMapping("/confirm")
    // public BaseResponse<PaymentResponse> payOrder(@RequestBody PaymentConfirmRequest request) {
    //     return new BaseResponse<>(paymentService.processPayment(request), "결제 요청이 정상적으로 처리되었습니다.");
    // }
}
