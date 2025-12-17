package com.domainservice.domain.order.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.ErrorResponse;
import com.domainservice.domain.order.model.dto.OrderStatusUpdateRequest;

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
    summary = "주문 상태 변경 (결제/환불)",
    description = """
        주문의 상태를 강제로 변경합니다. 주로 결제 서비스(Payment)의 콜백이나 내부 시스템에서 호출합니다.
        
        - **지원 상태**: `PAYMENT_COMPLETED` (결제 완료), `REFUND_AFTER_PAYMENT` (환불)
        - **동작**: 상태 변경과 함께 결제 ID(`paymentId`)를 기록하고, 상품의 재고 상태(`SOLDOUT` / `PROCESSING`)를 동기화합니다.
        """
)
@Parameters({
    @Parameter(
        name = "orderId",
        description = "대상 주문 ID",
        required = true,
        in = ParameterIn.PATH
    ),
    @Parameter(
        name = "userId",
        description = "사용자 ID (검증용)",
        required = true,
        in = ParameterIn.PATH
    )
})
@RequestBody(
    description = "상태 변경 요청 정보",
    required = true,
    content = @Content(
        schema = @Schema(implementation = OrderStatusUpdateRequest.class),
        examples = @ExampleObject(
            value = """
                {
                    "orderStatus": "PAYMENT_COMPLETED",
                    "paymentId": "toss_payment_key_1234"
                }
                """
        )
    )
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "상태 변경 성공",
        content = @Content(
            examples = @ExampleObject(
                value = "{}" // ResponseEntity<Void> 이므로 빈 객체 표현
            )
        )
    ),
    @ApiResponse(
        responseCode = "404",
        description = "주문을 찾을 수 없거나 지원하지 않는 상태 변경 요청",
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
public @interface UpdateOrderStatusApiDocs {
}