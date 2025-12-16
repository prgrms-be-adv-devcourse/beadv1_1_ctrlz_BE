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
    summary = "주문 부분 취소",
    description = """
        주문에 포함된 특정 아이템(상품) 하나를 취소합니다.
        
        - **결제 대기**: 해당 아이템이 취소(CANCELLED)됩니다.
        - **결제 완료**: 해당 아이템이 환불(REFUND_AFTER_PAYMENT) 처리됩니다.
        - **결과 반환**: 취소된 아이템은 응답의 `orderItems` 리스트에서 **제외**됩니다.
        - 모든 아이템이 취소되면 주문 상태도 취소/환불로 변경됩니다.
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
        description = "주문 ID",
        required = true,
        in = ParameterIn.PATH
    ),
    @Parameter(
        name = "orderItemId",
        description = "취소할 주문 아이템 ID (OrderItem PK)",
        required = true,
        in = ParameterIn.PATH
    )
})
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "부분 취소 성공 (취소된 아이템은 리스트에서 사라짐)",
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
                            "orderStatus": "PAYMENT_PENDING",
                            "orderItems": [
                                {
                                    "orderItemId": "order-item-2",
                                    "price": 15000,
                                    "status": "PAYMENT_PENDING"
                                }
                            ]
                        },
                        "message": "주문 일부 취소 성공했습니다"
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "취소 불가능한 상태",
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
        responseCode = "404",
        description = "주문 또는 아이템을 찾을 수 없음",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 404,
                        "message": "해당 주문 아이템을 찾을 수 없습니다."
                    }
                    """
            )
        )
    )
})
public @interface CancelOrderItemApiDocs {
}