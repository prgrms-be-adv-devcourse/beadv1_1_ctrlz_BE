package com.auth.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.web.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "로그아웃",
    description = """
        사용자를 로그아웃 처리합니다.
        
        1. **Server-side**: Redis에 저장된 Refresh Token을 삭제하여 재발급을 차단합니다.
        2. **Client-side**: 응답 헤더(`Set-Cookie`)를 통해 브라우저의 쿠키를 만료(삭제)시킵니다.
        """,
    security = @SecurityRequirement(name = "bearerAuth")
)
@Parameters ({
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
        name = "Authorization",
        description = "Bearer Access Token",
        required = true,
        in = ParameterIn.COOKIE,
        schema = @Schema(type = "string")
    )
})
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "로그아웃 성공 (쿠키 만료 처리됨)",
        headers = {
            @Header(name = "Set-Cookie", description = "Max-Age=0으로 설정된 만료 쿠키")
        },
        content = @Content(
            examples = @ExampleObject(value = "{}")
        )
    ),
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 401,
                        "message": "유효하지 않은 토큰입니다."
                    }
                    """
            )
        )
    )
})
public @interface LogoutApiDocs {
}