package com.user.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.ErrorResponse;
import com.user.infrastructure.api.dto.UserUpdateRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "회원 정보 수정",
    description = """
        사용자의 프로필 정보(닉네임, 전화번호, 주소)를 수정합니다.
        
        - **수정 가능 항목**: 닉네임, 전화번호, 주소(도/시/도로명/우편번호/상세)
        - **동작**: 입력된 값으로 기존 정보를 덮어씁니다.
        """
)
@Parameters({
    @Parameter(
        name = "X-REQUEST-ID",
        description = "사용자 ID",
        required = true,
        in = ParameterIn.HEADER,
        schema = @Schema(type = "string")
    )
})
@RequestBody(
    description = "수정할 회원 정보",
    required = true,
    content = @Content(
        schema = @Schema(implementation = UserUpdateRequest.class),
        examples = @ExampleObject(
            value = """
                {
                    "nickname": "new_nickname_123",
                    "phoneNumber": "010-9876-5432",
                    "state": "서울특별시",
                    "city": "강남구",
                    "street": "테헤란로 123",
                    "zipCode": "06234",
                    "details": "현대타워 8층"
                }
                """
        )
    )
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "정보 수정 완료 (반환값 없음)",
        content = @Content(
            examples = @ExampleObject(value = "{}")
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "입력값 유효성 검증 실패 (중복된 닉네임/전화번호 등)",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 400,
                        "message": "이미 존재하는 닉네임입니다."
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "404",
        description = "사용자를 찾을 수 없음",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 404,
                        "message": "해당 사용자를 찾을 수 없습니다."
                    }
                    """
            )
        )
    )
})
public @interface UpdateUserApiDocs {
}