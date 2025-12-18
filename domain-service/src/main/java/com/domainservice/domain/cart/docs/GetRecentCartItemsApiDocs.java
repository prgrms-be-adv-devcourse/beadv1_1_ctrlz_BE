package com.domainservice.domain.cart.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.ErrorResponse;
import com.domainservice.domain.cart.model.dto.response.CartItemResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "최근 장바구니 활동 조회",
    description = """
        장바구니에 추가되거나 변경된 아이템 목록을 조회합니다.
        마케팅이나 추천 시스템에 활용될 수 있습니다.
        """
)
@Parameter(
    name = "userId",
    description = "조회할 사용자 ID",
    required = true,
    in = ParameterIn.PATH
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = CartItemResponse.class)),
            examples = @ExampleObject(
                value = """
                    [
                        {
                            "cartItemId": "item-uuid-1",
                            "title": "강아지 사료",
                            "name": "유기농 닭고기 사료 1kg",
                            "price": 15000,
                            "isSelected": true
                        },
                        {
                            "cartItemId": "item-uuid-3",
                            "title": "고양이 간식",
                            "name": "츄르 참치맛",
                            "price": 3000,
                            "isSelected": false
                        }
                    ]
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "404",
        description = "사용자를 찾을 수 없는 경우",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 404,
                        "message": "존재하지 않는 사용자입니다."
                    }
                    """
            )
        )
    )
})
public @interface GetRecentCartItemsApiDocs {
}