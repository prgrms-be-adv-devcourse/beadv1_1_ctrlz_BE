package com.domainservice.domain.payment.api;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.domainservice.domain.deposit.service.DepositService;
import com.domainservice.domain.payment.model.dto.PaymentReadyResponse;
import com.domainservice.domain.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller // View 렌더링을 위한 @Controller
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentViewController {

    private final PaymentService paymentService;
    private final DepositService depositService;

    /** 결제 페이지 렌더링 */
    @GetMapping("/checkout/{orderId}")
    public String showCheckout(
        @PathVariable("orderId") String orderId, Model model) {
        PaymentReadyResponse paymentReady = paymentService.getPaymentReadyInfo(orderId);

        model.addAttribute("orderId", paymentReady.orderId());
        model.addAttribute("amount", paymentReady.amount());
        model.addAttribute("depositBalance", paymentReady.depositBalance());
        model.addAttribute("orderName", paymentReady.orderName());

        return "checkout";
    }

    @GetMapping("/success")
    public String showSuccess(
        @RequestParam String orderId,
        @RequestParam String orderName,
        @RequestParam String usedDepositAmount, //사용한 예치금
        @RequestParam String totalAmount,       //실제로 결제한 돈
        Model model) {

        model.addAttribute("orderId", orderId);
        model.addAttribute("orderName", orderName);
        model.addAttribute("usedDepositAmount", new BigDecimal(usedDepositAmount));
        model.addAttribute("totalAmount", new BigDecimal(totalAmount));

        return "success";
    }

    @GetMapping("/fail")
    public String showFail(
        @RequestParam String orderId,
        @RequestParam String orderName,
        @RequestParam String usedDepositAmount,
        @RequestParam String totalAmount,
        Model model) {
        model.addAttribute("orderId", orderId);
        model.addAttribute("orderName", orderName);
        model.addAttribute("usedDepositAmount", new BigDecimal(usedDepositAmount));
        model.addAttribute("totalAmount", new BigDecimal(totalAmount));

        return "fail";
    }

}
