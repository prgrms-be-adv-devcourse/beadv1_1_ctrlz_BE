package com.domainservice.domain.cart.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "장바구니 생성 [내부 API]",
    description = """
        사용자의 장바구니를 최초 생성합니다.
        회원가입 직후나 장바구니가 없는 유저가 접근할 때 호출됩니다.
        """
)
@RequestBody(
    description = "장바구니 생성 요청 객체",
    required = true,
    content = @Content(
        schema = @Schema(implementation = com.domainservice.domain.cart.model.dto.CreateCartRequest.class),
        examples = @ExampleObject(
            value = """
                {
                    "userId": "user-uuid-1234"
                }
                """
        )
    )
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "생성 성공 (반환값 없음)"
    ),
    @ApiResponse(
        responseCode = "400",
        description = "이미 장바구니가 존재하는 경우",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 400,
                        "message": "이미 장바구니가 존재하는 사용자입니다."
                    }
                    """
            )
        )
    )
})
public @interface CreateCartApiDocs {
}