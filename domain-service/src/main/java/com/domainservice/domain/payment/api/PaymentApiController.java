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
import com.domainservice.domain.payment.model.entity.PaymentEntity;
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
    // @PostMapping("/confirm")
    // public BaseResponse<PaymentResponse> confirmPayment(@RequestBody PaymentConfirmRequest request) {
    //     try {
    //         PaymentResponse response = paymentService.processPayment(request);
    //         return new BaseResponse<>(response, "결제 처리 완료");
    //     } catch (Exception e) {
    //         log.error("결제 처리 오류", e);
    //         return new BaseResponse<>(null, "결제 실패: " + e.getMessage());
    //     }
    // }


    // @PostMapping("/confirm")
    // public String confirmPayment(@RequestBody PaymentConfirmRequest request) {
    //     //TODO: 결제 승인 요청을 toss한테 보내야 됨.

    PaymentEntity paymentEntity;
    //
    // 	// 컨트롤러
    // 	/** 토스 결제 승인 요청 */
    // 	if (tossChargedAmount > 0) {
    // 		try {
    // 			String key = (secretApiKey != null) ? secretApiKey : "test_secret_key";
    // 			String authHeader = "Basic " + Base64.getEncoder()
    // 				.encodeToString((key + ":").getBytes(StandardCharsets.UTF_8));
    //
    // 			// Toss로 보내야하는 필수 필드
    // 			Map<String, Object> requestBody = Map.of(
    // 				"paymentKey", request.paymentKey(),
    // 				"orderId", request.orderId(),
    // 				"amount", request.amount()
    // 			);
    //
    // 			Map<String, Object> responseMap = paymentClient.requestPayment(
    // 				requestBody, authHeader, "application/json"
    // 			);
    //
    // 			log.info("결제 승인 성공: {}", requestBody);
    //
    // 			paymentEntity = PaymentEntity.of(
    // 				request.userId(),
    // 				request.orderId(),
    // 				request.amount(),
    // 				depositUsedAmount,
    // 				tossChargedAmount,
    // 				(String)responseMap.get("currency"),
    // 				payType,
    // 				(String)responseMap.get("status"),
    // 				(String)responseMap.get("paymentKey"),
    // 				(String)responseMap.get("failureReason"),
    // 				responseMap.get("approvedAt") != null
    // 					? LocalDateTime.parse((String)responseMap.get("approvedAt"))
    // 					: null
    // 			);
    //
    // 		} catch (Exception e) {
    // 			log.error("결제 승인 실패: {}", e.getMessage());
    // 			throw new CustomException(PaymentExceptionCode.PAYMENT_GATEWAY_FAILED.getMessage());
    // 		}
// } else {
// 		// 없으면 예외 터트리기
// 		);
//
// 	}
// 	paymentRepository.save(paymentEntity);
// 	return PaymentResponse.from(paymentEntity);

    //
    //     try {
    //         PaymentResponse response = paymentService.processPayment(request);
    //
    //         if (response.isSuccess()) {
    //             log.info("결제 성공: {}", request.orderId());
    //             return "redirect:/api/payments/success?orderId=" + request.orderId();
    //         } else {
    //             log.info("결제 실패: {}", request.orderId());
    //             return "redirect:/api/payments/fail?orderId=" + request.orderId();
    //         }
    //     } catch (Exception e) {
    //         log.error("결제 처리 중 오류 발생", e);
    //         return "redirect:/api/payments/fail?orderId=" + request.orderId();
    //     }
    // }

    /** 결제 준비 정보 조회 */
    @GetMapping("/ready/{orderId}")
    public BaseResponse<PaymentReadyResponse> getPaymentReadyInfo(@PathVariable String orderId) {
        return new BaseResponse<>(paymentService.getPaymentReadyInfo(orderId), "결제 요청이 정상적으로 처리되었습니다.");
    }

}
