package com.paymentservice.deposit.api;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/api/deposits")
@RequiredArgsConstructor
public class DepositViewController {

    /**
     * 결제 페이지 렌더링
     * - 프론트에서 amount(충전금액)를 전달하면 해당 값을 model에 넣어 결제 UI에서 사용 가능하게 함
     * - amount는 선택적이며 없으면 null (사용자가 직접 입력하거나 선택하도록 UI 구성)
     */

    @GetMapping("/charge")
    public String showCharge(
        @RequestParam(name = "amount", required = false) String amount,
        Model model) {

        // 프론트에서 보낸 amount가 있으면 전달
        if (amount != null) {
            model.addAttribute("amount", new BigDecimal(amount));
        }
        return "charge";
    }

    @GetMapping("/success")
    public String showSuccess() {
        return "depositSuccess";
    }

    @GetMapping("/fail")
    public String showFail() {
        return "depositFail";
    }

}
