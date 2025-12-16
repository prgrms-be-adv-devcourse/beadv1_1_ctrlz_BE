package com.domainservice.domain.order.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.PageResponse;

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
    summary = "주문 목록 조회 (페이징)",
    description = """
        사용자의 주문 내역을 페이징하여 조회합니다.
        
        ### 페이징 파라미터 (Query)
        - `page`: 페이지 번호 (0부터 시작)
        - `size`: 페이지 당 항목 수 (기본값: 10)
        - `sort`: 정렬 기준 (예: `createdAt,desc`)
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
    )
})
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(
            schema = @Schema(implementation = PageResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "pageNumber": 0,
                        "totalPages": 5,
                        "pageSize": 10,
                        "hasNext": true,
                        "content": [
                            {
                                "orderName": "강아지 사료 외 1건",
                                "orderId": "order-uuid-1",
                                "buyerId": "user-uuid-123",
                                "orderDate": "2024-12-15T14:30:00",
                                "totalAmount": 45000,
                                "orderStatus": "PAYMENT_COMPLETED",
                                "orderItems": [
                                    {
                                        "orderItemId": "item-1",
                                        "price": 30000,
                                        "status": "PAYMENT_COMPLETED"
                                    }
                                ]
                            },
                            {
                                "orderName": "고양이 장난감",
                                "orderId": "order-uuid-2",
                                "buyerId": "user-uuid-123",
                                "orderDate": "2024-12-10T11:00:00",
                                "totalAmount": 12000,
                                "orderStatus": "PURCHASE_CONFIRMED",
                                "orderItems": []
                            }
                        ]
                    }
                    """
            )
        )
    )
})
public @interface GetOrderListApiDocs {
}