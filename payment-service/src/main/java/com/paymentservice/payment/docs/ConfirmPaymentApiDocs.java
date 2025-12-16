package com.paymentservice.payment.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.BaseResponse;
import com.common.model.web.ErrorResponse;
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
    summary = "결제 승인 요청 (Toss / 복합 결제)",
    description = """
        프론트엔드(Toss Widget) 인증 후 최종 결제 승인을 요청합니다.
        
        ### 처리 과정
        1. **멱등성 체크**: 이미 처리된 `orderId`인 경우 기존 결제 정보를 반환합니다.
        2. **사전 검증**: 주문 금액과 결제 요청 금액의 일치 여부를 확인합니다.
        3. **복합 결제**: `usedDepositAmount`가 있는 경우 예치금을 먼저 차감하고, 남은 차액만 Toss로 승인 요청합니다.
        4. **최종 승인**: Toss 승인 결과를 저장하고 주문 상태를 `PAYMENT_COMPLETED`로 동기화합니다.
        """
)
@Parameters({
    @Parameter(
        name = "X-REQUEST-ID",
        required = false,
        in = ParameterIn.HEADER,
        schema = @Schema(
            type = "string",
            example = "gateway에서 전달되는 custom header값"
        )
    )
})
@RequestBody(
    description = "결제 승인 요청 정보",
    required = true,
    content = @Content(
        schema = @Schema(implementation = PaymentConfirmRequest.class),
        examples = @ExampleObject(
            value = """
                {
                    "paymentKey": "5zJ4xY7m0Kq...",
                    "orderId": "order-uuid-1234",
                    "amount": 50000,
                    "usedDepositAmount": 10000
                }
                """
        )
    )
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "결제 처리 완료 (성공 또는 기처리 건)",
        content = @Content(
            schema = @Schema(implementation = BaseResponse.class),
            examples = {
                @ExampleObject(
                    name = "성공 Case",
                    description = "결제가 정상적으로 승인된 경우",
                    value = """
                        {
                            "data": {
                                "userId": "user-uuid-123",
                                "paymentKey": "5zJ4xY7m0Kq...",
                                "orderId": "order-uuid-1234",
                                "amount": 50000,
                                "depositUsedAmount": 10000,
                                "tossChargedAmount": 40000,
                                "currency": "KRW",
                                "payType": "DEPOSIT_TOSS",
                                "status": "SUCCESS",
                                "approvedAt": "2024-12-15T16:00:00+09:00"
                            },
                            "message": "결제 처리 완료"
                        }
                        """
                ),
                @ExampleObject(
                    name = "기처리 Case (멱등성)",
                    description = "이미 승인된 주문 번호로 재요청한 경우",
                    value = """
                        {
                            "data": {
                                "userId": "user-uuid-123",
                                "paymentKey": "5zJ4xY7m0Kq...",
                                "orderId": "order-uuid-1234",
                                "amount": 50000,
                                "depositUsedAmount": 10000,
                                "tossChargedAmount": 40000,
                                "currency": "KRW",
                                "payType": "DEPOSIT_TOSS",
                                "status": "SUCCESS",
                                "approvedAt": "2024-12-15T16:00:00+09:00"
                            },
                            "message": "이미 처리된 결제입니다."
                        }
                        """
                )
            }
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "검증 실패 (금액 불일치 등)",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 400,
                        "message": "결제 실패: 주문 금액과 요청 금액이 일치하지 않습니다."
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "502",
        description = "PG사 연동 오류",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 502,
                        "message": "결제 실패: Toss Payments Gateway 오류"
                    }
                    """
            )
        )
    )
})
public @interface ConfirmPaymentApiDocs {
}