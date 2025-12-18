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
    summary = "장바구니 아이템 선택/해제",
    description = """
        장바구니에 담긴 특정 아이템의 선택 상태(체크박스)를 변경합니다.
        선택된 아이템만 주문 페이지로 넘어갑니다.
        """
)
@Parameters({
    @Parameter(
        name = "itemId",
        description = "장바구니 아이템 ID (CartItem PK)",
        required = true,
        in = ParameterIn.PATH
    ),
    @Parameter(
        name = "selected",
        description = "선택 여부 (true: 선택, false: 해제)",
        required = true,
        in = ParameterIn.QUERY
    )
})
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "상태 변경 성공",
        content = @Content(
            schema = @Schema(implementation = BaseResponse.class),
            examples = {
                @ExampleObject(
                    name = "Case 1. 선택(True)",
                    summary = "아이템을 선택했을 때",
                    value = """
                        {
                            "data": {
                                "cartItemId": "item-uuid-1",
                                "title": "강아지 사료",
                                "name": "유기농 닭고기 사료 1kg",
                                "price": 15000,
                                "isSelected": true
                            },
                            "message": "장바구니에서 체크선택 했습니다"
                        }
                        """
                ),
                @ExampleObject(
                    name = "Case 2. 해제(False)",
                    summary = "아이템 선택을 해제했을 때",
                    value = """
                        {
                            "data": {
                                "cartItemId": "item-uuid-1",
                                "title": "강아지 사료",
                                "name": "유기농 닭고기 사료 1kg",
                                "price": 15000,
                                "isSelected": false
                            },
                            "message": "장바구니에서 체크해제했습니다"
                        }
                        """
                )
            }
        )
    ),
    @ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 장바구니 아이템",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 404,
                        "message": "해당 장바구니 아이템을 찾을 수 없습니다."
                    }
                    """
            )
        )
    )
})
public @interface ToggleItemSelectionApiDocs {
}