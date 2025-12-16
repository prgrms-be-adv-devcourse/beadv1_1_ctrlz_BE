package com.domainservice.domain.cart.docs;

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
    summary = "장바구니 상품 추가",
    description = """
        특정 상품을 장바구니에 추가합니다.
        
        - **주의**: 이미 장바구니에 담긴 상품을 다시 추가하면 **에러(400)**가 발생합니다. (현재 수량 증가 로직 미적용)
        
        ### 인증 (Header)
        - **`X-REQUEST-ID`**: API Gateway에서 검증된 사용자 UUID (필수)
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
        name = "productPostId",
        description = "장바구니에 담을 상품 게시글 ID",
        required = true,
        in = ParameterIn.QUERY
    )
})
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "장바구니 추가 성공",
        content = @Content(
            schema = @Schema(implementation = BaseResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "data": {
                            "cartItemId": "item-uuid-new",
                            "title": "새로운 장난감",
                            "name": "자동 낚시대",
                            "price": 25000,
                            "isSelected": true
                        },
                        "message": "장바구니 아이템 추가 성공했습니다"
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "이미 장바구니에 존재하는 상품인 경우",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 400,
                        "message": "이미 장바구니에 담긴 상품입니다."
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "404",
        description = "장바구니가 없거나 상품을 찾을 수 없는 경우",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 404,
                        "message": "해당 상품을 찾을 수 없습니다."
                    }
                    """
            )
        )
    )
})
public @interface AddItemToCartApiDocs {
}