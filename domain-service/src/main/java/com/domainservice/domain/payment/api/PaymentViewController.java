package com.domainservice.domain.payment.api;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.domainservice.domain.payment.model.dto.PaymentReadyResponse;
import com.domainservice.domain.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller // View 렌더링을 위한 @Controller
@RequestMapping("/api/payments") // 일반적인 View 경로 (api 접두사 없음)
@RequiredArgsConstructor
public class PaymentViewController {

    private final PaymentService paymentService;

    /** 결제 페이지 렌더링 */
    @GetMapping("/checkout/{orderId}")
    public String showCheckout(@PathVariable("orderId") String orderId, Model model) {

		log.info("Showing checkout for order {}", orderId);
        PaymentReadyResponse paymentReady = paymentService.getPaymentReadyInfo(orderId);

        model.addAttribute("orderId", paymentReady.orderId());
        model.addAttribute("amount", paymentReady.amount());
        model.addAttribute("depositBalance", paymentReady.depositBalance());
        model.addAttribute("orderName", paymentReady.orderName());

        return "checkout";
    }

    @GetMapping("/request-confirm")
    public String showSuccess(
        @RequestParam String orderId,
        @RequestParam String orderName,
        @RequestParam Long amount,
        Model model) {
        model.addAttribute("orderId", orderId);
        model.addAttribute("orderName", orderName);
        model.addAttribute("amount", amount);
        return "success";
    }

    @GetMapping("/fail")
    public String showFail(
        @RequestParam String orderId,
        @RequestParam String orderName,
        @RequestParam Long amount,
        Model model) {
        model.addAttribute("orderId", orderId);
        model.addAttribute("orderName", orderName);
        model.addAttribute("amount", amount);

        // 차감했던 예치금 다시 더하기


        return "fail";
    }

}
