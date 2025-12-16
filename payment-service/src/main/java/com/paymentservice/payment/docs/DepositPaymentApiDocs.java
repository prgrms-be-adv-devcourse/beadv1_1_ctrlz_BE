package com.paymentservice.payment.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.BaseResponse;
import com.paymentservice.payment.model.dto.PaymentConfirmRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "예치금 전액 결제",
    description = """
        외부 PG(Toss) 연동 없이 **전액 예치금**으로만 결제를 진행합니다.
        
        ### 처리 과정
        1. **멱등성 체크**: 중복 주문 건인지 확인합니다.
        2. **금액 검증**: 주문 총액과 예치금 사용액이 일치하는지 확인합니다.
        3. **내부 트랜잭션**: 예치금 차감 및 결제 정보(`PayType.DEPOSIT`)를 저장합니다.
        """
)
@Parameters({
    @Parameter(
        name = "X-REQUEST-ID",
        description = "사용자 ID",
        required = true,
        in = ParameterIn.HEADER,
        schema = @Schema(type = "string")
    )
})
@RequestBody(
    description = "결제 요청 정보 (orderId, totalAmount, usedDepositAmount 등)",
    required = true,
    content = @Content(
        schema = @Schema(implementation = PaymentConfirmRequest.class),
        examples = @ExampleObject(
            value = """
                {
                    "orderId": "order-uuid-9999",
                    "amount": 10000,
                    "usedDepositAmount": 10000,
                    "paymentKey": "internal-deposit-key"
                }
                """
        )
    )
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "요청 처리 완료 (성공 또는 실패)",
        content = @Content(
            schema = @Schema(implementation = BaseResponse.class),
            examples = {
                @ExampleObject(
                    name = "성공 Case",
                    description = "예치금 결제 성공",
                    value = """
                        {
                            "data": {
                                "userId": "user-uuid-123",
                                "paymentKey": "payment-key-generated",
                                "orderId": "order-uuid-9999",
                                "amount": 10000,
                                "depositUsedAmount": 10000,
                                "tossChargedAmount": 0,
                                "currency": "KRW",
                                "payType": "DEPOSIT",
                                "status": "SUCCESS",
                                "approvedAt": "2024-12-15T16:30:00+09:00"
                            },
                            "message": "결제 처리 완료"
                        }
                        """
                ),
                @ExampleObject(
                    name = "기처리 Case",
                    description = "이미 처리된 결제인 경우",
                    value = """
                        {
                            "data": {
                                "userId": "user-uuid-123",
                                "paymentKey": "payment-key-generated",
                                "orderId": "order-uuid-9999",
                                "amount": 10000,
                                "depositUsedAmount": 10000,
                                "tossChargedAmount": 0,
                                "currency": "KRW",
                                "payType": "DEPOSIT",
                                "status": "SUCCESS",
                                "approvedAt": "2024-12-15T16:30:00+09:00"
                            },
                            "message": "이미 처리된 결제입니다."
                        }
                        """
                ),
                @ExampleObject(
                    name = "실패 Case",
                    description = "예치금 잔액 부족 또는 시스템 오류 (data가 null)",
                    value = """
                        {
                            "data": null,
                            "message": "결제 실패: 예치금 잔액이 부족합니다."
                        }
                        """
                )
            }
        )
    )
})
public @interface DepositPaymentApiDocs {
}