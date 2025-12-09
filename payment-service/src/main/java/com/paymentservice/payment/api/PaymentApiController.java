package com.paymentservice.payment.api;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.paymentservice.deposit.model.entity.Deposit;
import com.paymentservice.payment.client.PaymentTossClient;
import com.paymentservice.payment.exception.PaymentNotFoundException;
import com.paymentservice.payment.model.dto.PaymentConfirmRequest;
import com.paymentservice.payment.model.dto.PaymentReadyResponse;
import com.paymentservice.payment.model.dto.PaymentResponse;
import com.paymentservice.payment.model.dto.RefundResponse;
import com.paymentservice.payment.model.dto.TossApprovalResponse;
import com.paymentservice.payment.model.entity.PaymentEntity;
import com.paymentservice.payment.repository.PaymentRepository;
import com.paymentservice.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentApiController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final PaymentTossClient paymentTossClient;

    /** 결제 승인 요청 처리 */
    @PostMapping("/confirm")
    public BaseResponse<PaymentResponse> confirmPayment(
        @RequestBody PaymentConfirmRequest request,
        @RequestHeader(value = "X-REQUEST-ID") String userId
    ) {
        try {
            // 사전 검증, 예치금 조회
            Deposit deposit = paymentService.validateBeforeApprove(request, userId);
            // 토스 외부 서버
            TossApprovalResponse approve = paymentTossClient.approve(request);
            // 승인 결과 db저장, 이벤트 발행
            PaymentResponse response =
                paymentService.completeTossPayment(request, userId, deposit, approve);

            return new BaseResponse<>(response, "결제 처리 완료");
        } catch (Exception e) {
            log.error("결제 처리 오류", e);
            return new BaseResponse<>(null, "결제 실패: " + e.getMessage());
        }
    }

    /** 예치금으로 결제 요청 */
    @PostMapping("/deposit")
    public BaseResponse<PaymentResponse> depositPayment(
        @RequestBody PaymentConfirmRequest request,
        @RequestHeader(value = "X-REQUEST-ID") String userId
    ) {
        try {
            PaymentResponse response = paymentService.depositPayment(request, userId);

            return new BaseResponse<>(response, "결제 처리 완료");
        } catch (Exception e) {
            log.error("결제 처리 오류", e);
            return new BaseResponse<>(null, "결제 실패: " + e.getMessage());
        }
    }

    /** 결제 준비 정보 조회 */
    @GetMapping("/ready/{orderId}")
    public BaseResponse<PaymentReadyResponse> getPaymentReadyInfo(
        @PathVariable String orderId,
        @RequestHeader(value = "X-REQUEST-ID") String userId
    ) {
        return new BaseResponse<>(paymentService.getPaymentReadyInfo(orderId, userId), "결제 요청이 정상적으로 처리되었습니다.");
    }

    /** 환불 처리 */
    @PostMapping("/refund/{orderId}")
    public BaseResponse<RefundResponse> refundPayment(
        @PathVariable String orderId,
        @RequestHeader(value = "X-REQUEST-ID") String userId
    ) {
        try {
            PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException());

            BigDecimal depositAmount = payment.getDepositUsedAmount();
            BigDecimal tossAmount = payment.getTossChargedAmount();

            boolean hasDeposit = depositAmount.compareTo(BigDecimal.ZERO) > 0;
            boolean hasToss = tossAmount.compareTo(BigDecimal.ZERO) > 0;

            RefundResponse refundResponse;

            if (hasDeposit && hasToss) {
                // Deposit + toss
                RefundResponse tossRefund = paymentTossClient.refund(payment);
                refundResponse = paymentService.refundTossDeposit(payment, userId, tossRefund);
            } else if (hasDeposit) {
                // deposit
                refundResponse = paymentService.refundDeposit(payment, userId);
            } else {
                // toss
                RefundResponse tossRefund = paymentTossClient.refund(payment);
                refundResponse = paymentService.refundToss(payment, userId, tossRefund);
            }
            return new BaseResponse<>(refundResponse, "환불 완료");

        } catch (Exception e) {
            log.error("환불 처리 오류", e);
            return new BaseResponse<>(null, "환불 실패: " + e.getMessage());
        }
    }
}
