package com.user.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.BaseResponse;
import com.common.model.web.ErrorResponse;
import com.user.infrastructure.api.dto.UserCreateRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "회원가입",
    description = """
        신규 회원을 등록합니다.
        
        ### 토큰 발급
        - 가입 성공 시 **Response Header(Set-Cookie)**를 통해 Access Token과 Refresh Token이 발급됩니다.
        """
)
@RequestBody(
    description = "회원가입 요청 정보",
    required = true,
    content = @Content(
        schema = @Schema(implementation = UserCreateRequest.class),
        examples = @ExampleObject(
            value = """
                {
                    "email": "test@example.com",
                    "password": "password123!",
                    "name": "홍길동",
                    "nickname": "dev_hong",
                    "phoneNumber": "010-1234-5678",
                    "age": 25,
                    "gender": "MALE",
                    "state": "경기도",
                    "city": "성남시",
                    "street": "분당구 판교역로",
                    "zipCode": "13529",
                    "details": "101동 1202호"
                }
                """
        )
    )
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "회원가입 성공 (헤더에 토큰 포함)",
        headers = {
            @Header(name = "Set-Cookie", description = "Access Token 및 Refresh Token 발급")
        },
        content = @Content(
            schema = @Schema(implementation = BaseResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "data": {
                            "userId": "user-uuid-1234",
                            "profileUrl": "https://default-image.url/default.png",
                            "nickname": "dev_hong"
                        },
                        "message": "가입 완료"
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "입력값 검증 실패 또는 중복 데이터 존재",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = {
                @ExampleObject(
                    name = "Case 1. 닉네임 중복",
                    description = "이미 존재하는 닉네임인 경우",
                    value = """
                        {
                            "code": 400,
                            "message": "이미 존재하는 닉네임입니다."
                        }
                        """
                ),
                @ExampleObject(
                    name = "Case 2. 전화번호 중복",
                    description = "이미 가입된 전화번호인 경우",
                    value = """
                        {
                            "code": 400,
                            "message": "이미 존재하는 전화번호입니다."
                        }
                        """
                )
            }
        )
    )
})
public @interface CreateUserApiDocs {
}