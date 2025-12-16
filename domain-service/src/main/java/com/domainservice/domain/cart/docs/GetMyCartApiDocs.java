package com.domainservice.domain.cart.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.BaseResponse;
import com.common.model.web.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "장바구니 조회",
    description = """
        현재 로그인한 사용자의 장바구니에 담긴 모든 아이템을 조회합니다.
        
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
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "장바구니 조회 성공",
        content = @Content(
            schema = @Schema(implementation = BaseResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "data": [
                            {
                                "cartItemId": "item-uuid-1",
                                "title": "강아지 사료",
                                "name": "유기농 닭고기 사료 1kg",
                                "price": 15000,
                                "isSelected": true
                            },
                            {
                                "cartItemId": "item-uuid-2",
                                "title": "고양이 장난감",
                                "name": "낚시대 장난감",
                                "price": 5000,
                                "isSelected": false
                            }
                        ],
                        "message": "장바구니 아이템 리스트 조회 성공했습니다"
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "404",
        description = "장바구니가 존재하지 않는 경우",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 404,
                        "message": "장바구니를 찾을 수 없습니다."
                    }
                    """
            )
        )
    )
})
public @interface GetMyCartApiDocs {
}