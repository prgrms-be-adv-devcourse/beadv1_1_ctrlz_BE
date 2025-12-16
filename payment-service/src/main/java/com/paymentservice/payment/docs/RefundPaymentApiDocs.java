package com.paymentservice.payment.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "결제 환불",
    description = """
        특정 주문 건에 대한 결제를 전액 환불합니다.
        
        ### 환불 로직 자동 분기
        1. **Toss 결제만 존재**: Toss Payments 취소 API 호출
        2. **예치금만 사용**: 내부 예치금 잔액 복구
        3. **복합 결제 (Toss + 예치금)**: Toss 취소 후 예치금 복구 순차 진행
        """
)
@Parameters({
    @Parameter(
        name = "X-REQUEST-ID",
        description = "사용자 ID",
        required = true,
        in = ParameterIn.HEADER,
        schema = @Schema(type = "string")
    ),
    @Parameter(
        name = "orderId",
        description = "환불할 주문 ID",
        required = true,
        in = ParameterIn.PATH
    )
})
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "환불 처리 완료 (성공 또는 실패 메시지)",
        content = @Content(
            schema = @Schema(implementation = BaseResponse.class),
            examples = {
                @ExampleObject(
                    name = "성공 Case",
                    description = "환불이 정상적으로 완료된 경우",
                    value = """
                        {
                            "data": {
                                "paymentKey": "5zJ4xY7m0Kq...",
                                "orderId": "order-uuid-1234",
                                "status": "CANCELED",
                                "approvedAt": "2024-12-15T15:00:00+09:00",
                                "canceledAt": "2024-12-16T10:00:00+09:00",
                                "cancels": [
                                    {
                                        "cancelReason": "사용자 요청 환불",
                                        "cancelAmount": 50000,
                                        "canceledAt": "2024-12-16T10:00:00+09:00"
                                    }
                                ]
                            },
                            "message": "환불 완료"
                        }
                        """
                ),
                @ExampleObject(
                    name = "실패 Case",
                    description = "이미 환불되었거나 외부 PG사 오류 등으로 실패 시 (data가 null)",
                    value = """
                        {
                            "data": null,
                            "message": "환불 실패: 이미 전액 취소된 결제입니다."
                        }
                        """
                )
            }
        )
    )
})
public @interface RefundPaymentApiDocs {
}