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

@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "Access Token 재발급 (Reissue)",
    description = """
        만료된 Access Token을 갱신하기 위해 Refresh Token을 사용합니다.
        
        - **입력**: 브라우저 쿠키에 저장된 `REFRESH_TOKEN`을 자동으로 읽어옵니다.
        - **검증**: Redis에 저장된 토큰과 일치 여부 및 유효기간을 확인합니다.
        - **결과**: 새로운 Access Token이 **Response Header(`Set-Cookie`)**에 담겨 반환됩니다.
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
        name = "REFRESH_TOKEN",
        description = "리프레시 토큰 (HttpOnly Cookie)",
        required = true,
        in = ParameterIn.COOKIE,
        schema = @Schema(type = "string")
    )
})
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "재발급 성공 (Set-Cookie 헤더 확인)",
        headers = {
            @Header(name = "Set-Cookie", description = "갱신된 Access Token")
        },
        content = @Content(
            examples = @ExampleObject(value = "{}")
        )
    ),
    @ApiResponse(
        responseCode = "401",
        description = "토큰 만료, 불일치 또는 유효하지 않음 (재로그인 필요)",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 401,
                        "message": "재 로그인이 필요합니다."
                    }
                    """
            )
        )
    )
})
public @interface RefreshTokenApiDocs {
}