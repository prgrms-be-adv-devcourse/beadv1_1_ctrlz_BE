package com.domainservice.domain.payment.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.exception.CustomException;
import com.common.exception.vo.PaymentExceptionCode;
import com.domainservice.domain.deposit.model.entity.Deposit;
import com.domainservice.domain.deposit.repository.DepositJpaRepository;
import com.domainservice.domain.deposit.service.DepositService;
import com.domainservice.domain.order.model.entity.Order;
import com.domainservice.domain.order.repository.OrderRepository;
import com.domainservice.domain.payment.client.PaymentClient;
import com.domainservice.domain.payment.model.dto.PaymentConfirmRequest;
import com.domainservice.domain.payment.model.dto.PaymentReadyResponse;
import com.domainservice.domain.payment.model.dto.PaymentResponse;
import com.domainservice.domain.payment.model.entity.PaymentEntity;
import com.domainservice.domain.payment.model.enums.PayType;
import com.domainservice.domain.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

	private final DepositService depositService;
	private final DepositJpaRepository depositJpaRepository;
	private final PaymentRepository paymentRepository;
	private final PaymentClient paymentClient;
	private final OrderRepository orderRepository;


	@Value("${payment.toss.test_secret_api_key}")
	private String secretApiKey;

	@Transactional
	public PaymentReadyResponse getPaymentReadyInfo(String orderId) {
		/** 주문 검증 */
		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new CustomException(PaymentExceptionCode.ORDER_NOT_FOUND.getMessage()));

		BigDecimal orderAmount = order.getTotalAmount();
		if (orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new CustomException(PaymentExceptionCode.INVALID_ORDER_AMOUNT.getMessage());
		}

		/** 유저 검증 */
		String userId = order.getBuyerId();
		if (userId == null || userId.isBlank()) {
			throw new CustomException(PaymentExceptionCode.INVALID_USER_ID.getMessage());
		}

		String orderName = order.getOrderName();

		/** 예치금 조회 */
		Deposit deposit = depositJpaRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(PaymentExceptionCode.DEPOSIT_NOT_FOUND.getMessage()));

		BigDecimal depositBalance = BigDecimal.valueOf(deposit.getBalance());

		return new PaymentReadyResponse(
			userId,
			orderId,
			orderAmount,
			depositBalance,
			orderName
		);
	}
	//
	// @Transactional
	// public PaymentResponse processPayment(PaymentConfirmRequest request) {
	//
	// 	/** 주문 검증 */
	// 	Order order = orderRepository.findById(request.orderId())
	// 		.orElseThrow(() -> new CustomException(PaymentExceptionCode.ORDER_NOT_FOUND.getMessage()));
	//
	// 	int orderTotalAmount = order.getTotalAmount();
	//
	// 	/** 예치금 조회 */
	// 	Deposit deposit = depositService.getDepositByUserId(request.userId());
	// 	if (deposit == null) {
	// 		throw new CustomException(PaymentExceptionCode.DEPOSIT_NOT_FOUND.getMessage());
	// 	}
	//
	// 	int depositUsedAmount = 0;
	// 	int tossChargedAmount = 0;
	//
	// 	/** 예치금 사용 시나리오 */
	// 	if (request.depositUsed() && deposit.getBalance() > 0) {
	// 		if (deposit.getBalance() >= orderTotalAmount) {
	// 			// 예치금이 충분 → 예치금으로 전체 결제
	// 			depositUsedAmount = Math.min(deposit.getBalance(), orderTotalAmount);
	// 			tossChargedAmount = 0;
	// 		} else {
	// 			// 예치금이 부족 → 예치금 전액 + 나머지 TossPayments 결제
	// 			depositUsedAmount = deposit.getBalance();
	// 			tossChargedAmount = orderTotalAmount - depositUsedAmount;
	// 		}
	// 	} else {
	// 		// 예치금이 없거나 사용하지 않음 → TossPayments로 전체 결제
	// 		depositUsedAmount = 0;
	// 		tossChargedAmount = orderTotalAmount;
	// 	}
	//
	// 	/** 실제 결제 총액 검증 */
	// 	int actualTotalPayment = depositUsedAmount + tossChargedAmount;
	// 	if (order.getTotalAmount() != actualTotalPayment) {
	// 		throw new CustomException(PaymentExceptionCode.INVALID_ORDER_AMOUNT.getMessage());
	// 	}
	// 	/** 예치금 차감 */
	// 	// API따로, 안쓸거면 프론트에서 계산하고 계산한거 서버로 옮겨라
	// 	if (depositUsedAmount > 0) {
	// 		deposit.decreaseBalance(depositUsedAmount);
	// 	}
	//
	// 	/** PayType 결정 */
	// 	PayType payType;
	// 	if (depositUsedAmount == orderTotalAmount) {
	// 		payType = PayType.DEPOSIT; // 예치금 전액 결제
	// 	} else if (depositUsedAmount > 0) {
	// 		payType = PayType.DEPOSIT_TOSS; // 일부 예치금 + 일부 TossPayments
	// 	} else {
	// 		payType = PayType.TOSS; // TossPayments만 사용
	// 	}
	//
	// 	PaymentEntity paymentEntity;
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
	// 	} else {
	// 		// 결제가 완료되면 하기
	// 		// 예치금만 결제
	// 		paymentEntity = PaymentEntity.of(
	// 			request.userId(),
	// 			request.orderId(),
	// 			request.amount(),
	// 			depositUsedAmount,
	// 			0,
	// 			"KRW",
	// 			payType,
	// 			"SUCCESS",
	// 			null,
	// 			null,
	// 			null
	// 		);
	//
	// 	}
	// 	paymentRepository.save(paymentEntity);
	// 	return PaymentResponse.from(paymentEntity);
	// 	return null;
	// }
}
