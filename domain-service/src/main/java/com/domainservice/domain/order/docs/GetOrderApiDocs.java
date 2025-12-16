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
    summary = "주문 상세 조회",
    description = """
        주문 ID를 통해 주문 상세 정보를 조회합니다.
        요청 헤더의 사용자 ID와 주문자 ID가 일치해야 조회할 수 있습니다.
        
        ### 인증 (Header)
        - **`X-REQUEST-ID`**: API Gateway에서 검증된 사용자 UUID (필수)
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
        description = "조회할 주문 ID",
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
                            "orderName": "강아지 사료 외 1건",
                            "orderId": "order-uuid-999",
                            "buyerId": "user-uuid-123",
                            "orderDate": "2024-12-15T14:30:00",
                            "totalAmount": 45000,
                            "orderStatus": "PAYMENT_COMPLETED",
                            "orderItems": [
                                {
                                    "orderItemId": "order-item-1",
                                    "price": 30000,
                                    "status": "PAYMENT_COMPLETED"
                                },
                                {
                                    "orderItemId": "order-item-2",
                                    "price": 15000,
                                    "status": "PAYMENT_COMPLETED"
                                }
                            ]
                        },
                        "message": "주문 상세 조회 성공했습니다"
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
                        "message": "해당 주문에 대한 접근 권한이 없습니다."
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
public @interface GetOrderApiDocs {
}