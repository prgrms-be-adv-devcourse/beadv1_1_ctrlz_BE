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
    summary = "장바구니 아이템 삭제",
    description = "장바구니에 담긴 특정 아이템을 삭제합니다."
)
@Parameter(
    name = "itemId",
    description = "삭제할 장바구니 아이템 ID (CartItem PK)",
    required = true,
    in = ParameterIn.PATH
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "삭제 성공",
        content = @Content(
            schema = @Schema(implementation = BaseResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "data": null,
                        "message": "삭제 완료"
                    }
                    """
            )
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
public @interface RemoveCartItemApiDocs {
}