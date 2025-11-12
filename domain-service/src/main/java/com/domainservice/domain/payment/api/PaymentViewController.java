package com.domainservice.domain.payment.api;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.domainservice.domain.payment.model.dto.PaymentReadyResponse;
import com.domainservice.domain.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller // View 렌더링을 위한 @Controller
@RequestMapping("/payments") // 일반적인 View 경로 (api 접두사 없음)
@RequiredArgsConstructor
public class PaymentViewController {

    private final PaymentService paymentService;

    /** 결제 페이지 렌더링 */
    @GetMapping("/checkout/{orderId}")
    public String showCheckout(@PathVariable("orderId") String orderId, Model model) {
        log.info("orderId{}:", orderId);
        PaymentReadyResponse paymentReady = paymentService.getPaymentReadyInfo(orderId);
        model.addAttribute("orderId", orderId);
        model.addAttribute("orderName", paymentReady.orderName());
        model.addAttribute("amount", paymentReady.amount());
        model.addAttribute("depositBalance", paymentReady.depositBalance());
        model.addAttribute("payPrice", paymentReady.amount()); // payPrice 초기값을 amount로 설정

        return "checkout";
    }

    /** 결제 성공 페이지 */
    @GetMapping("/success")
    public String showSuccess(@RequestParam(required = false) String orderId, Model model) {

        // orderId가 null일 경우, 예외 처리 필요
        // if (orderId == null || orderId.isBlank()) {
        //     // 적절한 오류 페이지 또는 기본 처리
        //     return "fail";
        // }

        model.addAttribute("orderId", orderId);
        model.addAttribute("orderName", paymentService.getPaymentReadyInfo(orderId).orderName());
        model.addAttribute("amount", paymentService.getPaymentReadyInfo(orderId).amount());

        return "success";
    }

    /** 결제 실패 페이지 */
    @GetMapping("/fail")
    public String showFail(@RequestParam(required = false) String orderId, Model model) {
        model.addAttribute("orderId", orderId);
        model.addAttribute("orderName", paymentService.getPaymentReadyInfo(orderId).orderName());
        model.addAttribute("amount", paymentService.getPaymentReadyInfo(orderId).amount());
        return "fail";
    }
}
