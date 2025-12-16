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
    summary = "주문 확정 (구매 확정)",
    description = """
        주문을 구매 확정(PURCHASE_CONFIRMED) 상태로 변경합니다.
        구매 확정 시 상품은 완전히 판매된 것으로 간주됩니다.
        
        - **전제 조건**: 주문 상태가 **결제 완료(PAYMENT_COMPLETED)**여야 합니다.
        - 결제 대기 중이거나 이미 취소된 주문은 확정할 수 없습니다.
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
        description = "확정할 주문 ID",
        required = true,
        in = ParameterIn.PATH
    )
})
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "주문 확정 성공",
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
                            "orderStatus": "PURCHASE_CONFIRMED",
                            "orderItems": [
                                {
                                    "orderItemId": "order-item-1",
                                    "price": 30000,
                                    "status": "PURCHASE_CONFIRMED"
                                },
                                {
                                    "orderItemId": "order-item-2",
                                    "price": 15000,
                                    "status": "PURCHASE_CONFIRMED"
                                }
                            ]
                        },
                        "message": "주문 확정 성공했습니다"
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "확정할 수 없는 상태 (결제 미완료 등)",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 400,
                        "message": "구매 확정이 불가능한 주문 상태입니다."
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
                        "message": "주문 확정 권한이 없습니다."
                    }
                    """
            )
        )
    )
})
public @interface ConfirmPurchaseApiDocs {
}