package com.paymentservice.payment.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.BaseResponse;
import com.common.model.web.ErrorResponse;

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
    summary = "정산용 결제 내역 조회 (Batch/Admin)",
    description = """
        특정 기간(Start ~ End) 동안 완료된 모든 결제 내역을 조회합니다.
        정산 시스템이나 데이터 분석을 위한 배치 작업에서 사용됩니다.
        
        ### 날짜 형식
        - **ISO 8601** 형식을 준수해야 합니다.
        - 예: `2024-12-01T00:00:00`
        """
)
@Parameters({
    @Parameter(
        name = "startDate",
        description = "조회 시작 일시 (yyyy-MM-dd'T'HH:mm:ss)",
        required = true,
        in = ParameterIn.QUERY,
        example = "2024-12-01T00:00:00"
    ),
    @Parameter(
        name = "endDate",
        description = "조회 종료 일시 (yyyy-MM-dd'T'HH:mm:ss)",
        required = true,
        in = ParameterIn.QUERY,
        example = "2024-12-31T23:59:59"
    )
})
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(
            schema = @Schema(implementation = BaseResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "data": [
                            {
                                "userId": "user-uuid-1",
                                "paymentKey": "toss_pay_key_1",
                                "orderId": "order-uuid-1",
                                "amount": 50000,
                                "depositUsedAmount": 10000,
                                "tossChargedAmount": 40000,
                                "currency": "KRW",
                                "payType": "DEPOSIT_TOSS",
                                "status": "SUCCESS",
                                "approvedAt": "2024-12-05T10:00:00+09:00"
                            },
                            {
                                "userId": "user-uuid-2",
                                "paymentKey": "deposit_pay_key_2",
                                "orderId": "order-uuid-2",
                                "amount": 15000,
                                "depositUsedAmount": 15000,
                                "tossChargedAmount": 0,
                                "currency": "KRW",
                                "payType": "DEPOSIT",
                                "status": "SUCCESS",
                                "approvedAt": "2024-12-06T14:30:00+09:00"
                            }
                        ],
                        "message": "정산 내역 조회 성공"
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 날짜 형식",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 400,
                        "message": "날짜 형식이 올바르지 않습니다."
                    }
                    """
            )
        )
    )
})
public @interface GetPaymentsForSettlementApiDocs {
}