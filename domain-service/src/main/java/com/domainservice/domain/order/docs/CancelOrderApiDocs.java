package com.domainservice.domain.order.docs;

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
    summary = "주문 전체 취소",
    description = """
        주문 전체를 취소합니다.
        
        - **결제 대기(PAYMENT_PENDING)** 상태: 즉시 **취소(CANCELLED)** 처리됩니다.
        - **결제 완료(PAYMENT_COMPLETED)** 상태: **환불(REFUND_AFTER_PAYMENT)** 상태로 변경되며 PG사 환불 로직이 수행됩니다.
        - **주의**: 구매 확정(PURCHASE_CONFIRMED)된 주문은 취소할 수 없습니다.
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
        description = "취소할 주문 ID",
        required = true,
        in = ParameterIn.PATH
    )
})
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "주문 취소 성공",
        content = @Content(
            schema = @Schema(implementation = BaseResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "data": {
                            "orderName": "강아지 사료 외 1건",
                            "orderId": "order-uuid-999",
                            "buyerId": "user-uuid-123",
                            "orderDate": "2024-12-15T14:30:00",
                            "totalAmount": 45000,
                            "orderStatus": "CANCELLED",
                            "orderItems": [
                                {
                                    "orderItemId": "order-item-1",
                                    "price": 30000,
                                    "status": "CANCELLED"
                                },
                                {
                                    "orderItemId": "order-item-2",
                                    "price": 15000,
                                    "status": "CANCELLED"
                                }
                            ]
                        },
                        "message": "주문 취소 성공했습니다"
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "취소 불가능한 상태 (이미 구매 확정됨)",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 400,
                        "message": "이미 구매 확정된 주문은 취소할 수 없습니다."
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
                        "message": "주문 취소 권한이 없습니다."
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
                        "message": "해당 주문을 찾을 수 없습니다."
                    }
                    """
            )
        )
    )
})
public @interface CancelOrderApiDocs {
}