package com.domainservice.domain.order.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.BaseResponse;
import com.common.model.web.ErrorResponse;
import com.domainservice.domain.order.model.dto.CreateOrderRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "주문 생성",
    description = """
        장바구니에 담긴 아이템 ID 목록을 받아 주문을 생성합니다.
        주문 상태는 **결제 대기(PAYMENT_PENDING)**로 초기화됩니다.
        
        ### 인증 (Header)
        - **`X-REQUEST-ID`**: API Gateway에서 검증된 사용자 UUID (필수)
        """
)
@Parameter(
    name = "X-REQUEST-ID",
    description = "사용자 ID",
    required = true,
    in = ParameterIn.HEADER,
    schema = @Schema(type = "string")
)
@RequestBody(
    description = "주문 생성 요청 (장바구니 아이템 ID 목록)",
    required = true,
    content = @Content(
        schema = @Schema(implementation = CreateOrderRequest.class),
        examples = @ExampleObject(
            value = """
                {
                    "cartItemIds": [
                        "cart-item-111",
                        "cart-item-222"
                    ]
                }
                """
        )
    )
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "주문 생성 성공",
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
                                    "orderItemId": "order-item-1",
                                    "price": 30000,
                                    "status": "PAYMENT_PENDING"
                                },
                                {
                                    "orderItemId": "order-item-2",
                                    "price": 15000,
                                    "status": "PAYMENT_PENDING"
                                }
                            ]
                        },
                        "message": "주문 생성 성공했습니다"
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "장바구니 아이템 정보가 유효하지 않음",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 400,
                        "message": "장바구니 아이템을 찾을 수 없습니다."
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "409",
        description = "주문하려는 상품이 판매 중이 아님",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 409,
                        "message": "판매 중이지 않은 상품이 포함되어 있습니다."
                    }
                    """
            )
        )
    )
})
public @interface CreateOrderApiDocs {
}