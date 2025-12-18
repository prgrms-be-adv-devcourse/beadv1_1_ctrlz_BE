package com.paymentservice.payment.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.paymentservice.deposit.model.entity.Deposit;
import com.paymentservice.payment.client.PaymentTossClient;
import com.paymentservice.payment.docs.ConfirmPaymentApiDocs;
import com.paymentservice.payment.docs.DepositPaymentApiDocs;
import com.paymentservice.payment.docs.GetPaymentReadyInfoApiDocs;
import com.paymentservice.payment.docs.GetPaymentsForSettlementApiDocs;
import com.paymentservice.payment.docs.RefundPaymentApiDocs;
import com.paymentservice.payment.exception.PaymentNotFoundException;
import com.paymentservice.payment.model.dto.PaymentConfirmRequest;
import com.paymentservice.payment.model.dto.PaymentReadyResponse;
import com.paymentservice.payment.model.dto.PaymentResponse;
import com.paymentservice.payment.model.dto.RefundResponse;
import com.paymentservice.payment.model.dto.TossApprovalResponse;
import com.paymentservice.payment.model.entity.PaymentEntity;
import com.paymentservice.payment.repository.PaymentRepository;
import com.paymentservice.payment.service.PaymentService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Payment", description = "결제 API")
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentApiController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final PaymentTossClient paymentTossClient;

    @ConfirmPaymentApiDocs
    @PostMapping("/confirm")
    public BaseResponse<PaymentResponse> confirmPayment(
        @RequestBody PaymentConfirmRequest request,
        @RequestHeader(value = "X-REQUEST-ID") String userId
    ) {
        TossApprovalResponse approve = null;
        try {
            // 멱등성체크
            if (paymentRepository.existsByOrderId(request.orderId())) {
                PaymentResponse existingPayment = paymentService.findByOrderId(request.orderId());
                return new BaseResponse<>(existingPayment, "이미 처리된 결제입니다.");
            }

            // 사전 검증, 예치금 조회
            Deposit deposit = paymentService.validateBeforeApprove(request, userId);
            // 토스 외부 서버
            approve = paymentTossClient.approve(request);
            // 승인 결과 db저장, 이벤트 발행
            PaymentResponse response =
                paymentService.completeTossPayment(request, userId, deposit, approve);

            return new BaseResponse<>(response, "결제 처리 완료");
        } catch (Exception e) {
            log.error("결제 처리 오류", e);

            // 토스 승인은 땄는데(approve != null), 내부 로직에서 터진 경우 -> 결제 취소
            if (approve != null) {
                log.warn("내부 처리 실패로 인한 결제 취소 진행: orderId={}, paymentKey={}", request.orderId(), approve.paymentKey());
                paymentTossClient.cancelPayment(approve.paymentKey(), "Internal Server Error: " + e.getMessage());
            }

            return new BaseResponse<>(null, "결제 실패: " + e.getMessage());
        }
    }

    @DepositPaymentApiDocs
    @PostMapping("/deposit")
    public BaseResponse<PaymentResponse> depositPayment(
        @RequestBody PaymentConfirmRequest request,
        @RequestHeader(value = "X-REQUEST-ID") String userId
    ) {
        try {
            // 멱등성체크
            if (paymentRepository.existsByOrderId(request.orderId())) {
                PaymentResponse existingPayment = paymentService.findByOrderId(request.orderId());
                return new BaseResponse<>(existingPayment, "이미 처리된 결제입니다.");
            }

            PaymentResponse response = paymentService.depositPayment(request, userId);
            return new BaseResponse<>(response, "결제 처리 완료");
        } catch (Exception e) {
            log.error("결제 처리 오류", e);
            return new BaseResponse<>(null, "결제 실패: " + e.getMessage());
        }
    }

    @GetPaymentReadyInfoApiDocs
    @GetMapping("/ready/{orderId}")
    public BaseResponse<PaymentReadyResponse> getPaymentReadyInfo(
        @PathVariable String orderId,
        @RequestHeader(value = "X-REQUEST-ID") String userId
    ) {
        return new BaseResponse<>(paymentService.getPaymentReadyInfo(orderId, userId), "결제 요청이 정상적으로 처리되었습니다.");
    }

    @RefundPaymentApiDocs
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

    @GetPaymentsForSettlementApiDocs
    @GetMapping("/settlement")
    public BaseResponse<java.util.List<PaymentResponse>> getPaymentsForSettlement(
        @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return new BaseResponse<>(
            paymentService.getPaymentsForSettlement(startDate, endDate),
            "정산 내역 조회 성공");
    }
}
