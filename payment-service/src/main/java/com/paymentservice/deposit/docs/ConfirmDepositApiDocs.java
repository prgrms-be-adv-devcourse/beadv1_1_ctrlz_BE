package com.paymentservice.deposit.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.BaseResponse;
import com.paymentservice.deposit.model.dto.DepositConfirmRequest;

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
    summary = "예치금 충전 확정 (Toss 결제 승인)",
    description = """
        Toss Payments 프론트엔드 위젯 인증 후, 최종 결제 승인 및 예치금 충전을 요청합니다.
        
        1. Toss 서버에 결제 승인 요청 (`approve`)
        2. 승인된 금액 검증
        3. 예치금 잔액 증가 및 로그 기록
        
        **주의**: 결제 실패 시에도 HTTP 200이 반환될 수 있으며, 이때 `data`는 null입니다.
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
    description = "충전 확정 요청 정보 (paymentKey, orderId, amount)",
    required = true,
    content = @Content(
        schema = @Schema(implementation = DepositConfirmRequest.class),
        examples = @ExampleObject(
            value = """
                {
                    "paymentKey": "5zJ4xY7m0Kq... (Toss Payment Key)",
                    "orderId": "deposit-order-uuid-1234",
                    "amount": 50000
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
                    description = "충전이 정상적으로 완료된 경우",
                    value = """
                        {
                            "data": {
                                "orderId": "deposit-order-uuid-1234",
                                "userId": "user-uuid-123",
                                "paymentKey": "5zJ4xY7m0Kq...",
                                "amount": 50000,
                                "currency": "KRW",
                                "approvedAt": "2024-12-15T15:30:00+09:00"
                            },
                            "message": "충전 완료"
                        }
                        """
                ),
                @ExampleObject(
                    name = "실패 Case",
                    description = "Toss 승인 실패 또는 금액 불일치 등 오류 발생 시 (data가 null임)",
                    value = """
                        {
                            "data": null,
                            "message": "충전 실패: 유효하지 않은 결제 금액입니다."
                        }
                        """
                )
            }
        )
    )
})
public @interface ConfirmDepositApiDocs {
}