package com.domainservice.domain.payment.api;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.exception.CustomException;
import com.common.exception.vo.PaymentExceptionCode;
import com.common.model.web.BaseResponse;
import com.domainservice.domain.deposit.service.DepositService;
import com.domainservice.domain.order.repository.OrderRepository;
import com.domainservice.domain.payment.model.dto.PaymentConfirmRequest;
import com.domainservice.domain.payment.model.dto.PaymentReadyResponse;
import com.domainservice.domain.payment.model.dto.PaymentResponse;
import com.domainservice.domain.payment.model.dto.RefundResponse;
import com.domainservice.domain.payment.model.entity.PaymentEntity;
import com.domainservice.domain.payment.repository.PaymentRepository;
import com.domainservice.domain.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentApiController {

    private final PaymentService paymentService;
    private final DepositService depositService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    /** 결제 승인 요청 처리 */
    @PostMapping("/confirm")
    public BaseResponse<PaymentResponse> confirmPayment(@RequestBody PaymentConfirmRequest request) {
        try {
            PaymentResponse response = paymentService.processPayment(request);
            return new BaseResponse<>(response, "결제 처리 완료");
        } catch (Exception e) {
            log.error("결제 처리 오류", e);
            return new BaseResponse<>(null, "결제 실패: " + e.getMessage());
        }
    }

    /** 예치금으로 결제 요청 */
    @PostMapping("/deposit")
    public BaseResponse<PaymentResponse> depositPayment(
        @RequestBody PaymentConfirmRequest request) {
        try {
            PaymentResponse response = paymentService.depositPayment(request);
            return new BaseResponse<>(response, "결제 처리 완료");
        } catch (Exception e) {
            log.error("결제 처리 오류", e);
            return new BaseResponse<>(null, "결제 실패: " + e.getMessage());
        }
    }

    /** 결제 준비 정보 조회 */
    @GetMapping("/ready/{orderId}")
    public BaseResponse<PaymentReadyResponse> getPaymentReadyInfo(
        @PathVariable String orderId) {
        return new BaseResponse<>(paymentService.getPaymentReadyInfo(orderId), "결제 요청이 정상적으로 처리되었습니다.");
    }

    /** 환불 처리 */
    @PostMapping("/refund/{orderId}")
    public BaseResponse<RefundResponse> refundPayment(@PathVariable String orderId) {
        try {
            PaymentEntity payment = paymentRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new CustomException(PaymentExceptionCode.PAYMENT_NOT_FOUND.getMessage()));

            boolean includeDeposit = payment.getDepositUsedAmount().compareTo(BigDecimal.ZERO) > 0;
            return new BaseResponse<>(paymentService.refundOrder(payment, includeDeposit), "환불 완료");
        } catch (Exception e) {
            log.error("환불 처리 오류", e);
            return new BaseResponse<>(null, "환불 실패: " + e.getMessage());
        }
    }

}
