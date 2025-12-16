package com.domainservice.domain.order.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.BaseResponse;

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
    summary = "정산용 주문 목록 조회 (배치/관리자)",
    description = """
        특정 기간(Start ~ End) 동안 생성된 모든 주문 내역을 조회합니다.
        주로 정산 시스템이나 배치(Batch) 작업에서 대량의 데이터를 수집할 때 사용됩니다.
        
        ### 날짜 형식
        - **ISO 8601** 형식을 따릅니다.
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
                                "orderName": "강아지 사료 외 1건",
                                "orderId": "order-uuid-1",
                                "buyerId": "user-uuid-A",
                                "orderDate": "2024-12-05T10:00:00",
                                "totalAmount": 45000,
                                "orderStatus": "PURCHASE_CONFIRMED",
                                "orderItems": []
                            },
                            {
                                "orderName": "고양이 캔 1box",
                                "orderId": "order-uuid-2",
                                "buyerId": "user-uuid-B",
                                "orderDate": "2024-12-05T11:30:00",
                                "totalAmount": 28000,
                                "orderStatus": "PAYMENT_COMPLETED",
                                "orderItems": []
                            }
                        ],
                        "message": "정산용 주문 목록 조회 성공했습니다"
                    }
                    """
            )
        )
    )
})
public @interface GetOrdersForSettlementApiDocs {
}