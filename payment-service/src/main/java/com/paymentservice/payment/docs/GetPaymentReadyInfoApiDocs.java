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
    summary = "결제 준비 정보 조회",
    description = """
        결제 페이지(Checkout) 진입 시 필요한 정보를 조회합니다.
        
        - **주요 반환 정보**: 주문 총 금액(`amount`), 현재 보유 예치금(`depositBalance`), 주문명(`orderName`)
        - 프론트엔드는 이 정보를 사용하여 결제 화면을 렌더링하고, 예치금 사용 여부를 결정합니다.
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
    ),
    @Parameter(
        name = "orderId",
        description = "주문 ID",
        required = true,
        in = ParameterIn.PATH
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
                        "data": {
                            "userId": "user-uuid-123",
                            "orderId": "order-uuid-999",
                            "amount": 50000,
                            "depositBalance": 12000,
                            "orderName": "강아지 사료 외 1건"
                        },
                        "message": "결제 요청이 정상적으로 처리되었습니다."
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "403",
        description = "본인의 주문이 아닌 경우",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 403,
                        "message": "해당 주문에 대한 권한이 없습니다."
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "404",
        description = "주문을 찾을 수 없음",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 404,
                        "message": "해당 주문 정보가 존재하지 않습니다."
                    }
                    """
            )
        )
    )
})
public @interface GetPaymentReadyInfoApiDocs {
}