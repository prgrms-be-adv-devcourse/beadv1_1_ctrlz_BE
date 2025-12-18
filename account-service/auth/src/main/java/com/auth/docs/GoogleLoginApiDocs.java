package com.auth.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.web.ErrorResponse;

import com.auth.dto.LoginRequest;
import com.auth.dto.LoginResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "토큰 발급 [내부 API]",
    description = """
        OAuth 제공자(Google)로부터 받은 정보를 이용해 인증을 합니다.
        
        ### 응답 분기 (`isNewUser` 필드 확인)
        1. **`isNewUser: false` (기존 회원)**:
           - 로그인 성공으로 간주합니다.
           - `accessToken`, `refreshToken`이 발급됩니다.
           
        2. **`isNewUser: true` (신규 회원)**:
           - 아직 회원가입이 완료되지 않은 상태입니다.
           - `accessToken`, `refreshToken`은 `null`입니다.
           - 반환된 이메일, 프로필 정보를 이용하여 **회원가입 API(`POST /api/users`)**를 호출해야 합니다.
        """
)
@RequestBody(
    description = "소셜 로그인 요청 정보",
    required = true,
    content = @Content(
        schema = @Schema(implementation = LoginRequest.class),
        examples = @ExampleObject(
            value = """
                {
                    "email": "test@gmail.com",
                    "nickname": "테스터",
                    "provider": "google",
                    "profileImageUrl": "https://lh3.googleusercontent.com/..."
                }
                """
        )
    )
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "요청 처리 성공 (로그인 또는 가입 유도)",
        content = @Content(
            schema = @Schema(implementation = LoginResponse.class),
            examples = {
                @ExampleObject(
                    name = "Case 1. 기존 회원 (로그인 성공)",
                    description = "토큰이 정상적으로 발급됩니다.",
                    value = """
                        {
                            "accessToken": "eyJhbGciOiJIUz...",
                            "refreshToken": "dGhpcyIsImlz...",
                            "userId": "user-uuid-1234",
                            "email": "test@gmail.com",
                            "nickname": "테스터",
                            "provider": "google",
                            "profileImageUrl": "https://lh3.google...",
                            "isNewUser": false
                        }
                        """
                ),
                @ExampleObject(
                    name = "Case 2. 신규 회원 (회원가입 필요)",
                    description = "토큰 없이 유저 정보만 반환됩니다. 회원가입 페이지로 이동시켜주세요.",
                    value = """
                        {
                            "accessToken": null,
                            "refreshToken": null,
                            "userId": null,
                            "email": "newbie@gmail.com",
                            "nickname": "뉴비",
                            "provider": "google",
                            "profileImageUrl": "https://lh3.google...",
                            "isNewUser": true
                        }
                        """
                )
            }
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 데이터",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 400,
                        "message": "지원하지 않는 Provider입니다."
                    }
                    """
            )
        )
    )
})
public @interface GoogleLoginApiDocs {
}