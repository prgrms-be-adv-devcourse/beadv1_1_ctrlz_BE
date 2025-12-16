package com.user.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.ErrorResponse;
import com.user.infrastructure.api.dto.VerificationReqeust;

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
    summary = "판매자 인증 코드 발송",
    description = """
        판매자 권한 신청을 위해 사용자의 휴대폰 번호로 6자리 인증 코드를 발송합니다.
        
        - **다음 단계**: 발송된 코드를 `/api/users/sellers` (판매자 등록) API에 입력하여 검증해야 합니다.
        """
)
@Parameters({
    @Parameter(
        name = "X-REQUEST-ID",
        required = false,
        in = ParameterIn.HEADER,
        schema = @Schema(
            type = "string",
            example = "gateway에서 전달되는 custom header값"
        )
    )
})
@RequestBody(
    description = "인증 번호를 받을 전화번호",
    required = true,
    content = @Content(
        schema = @Schema(implementation = VerificationReqeust.class),
        examples = @ExampleObject(
            value = """
                {
                    "phoneNumber": "010-1234-5678"
                }
                """
        )
    )
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "인증 코드 발송 성공 (반환값 없음)",
        content = @Content(
            examples = @ExampleObject(value = "{}")
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 전화번호 형식 또는 발송 실패",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 400,
                        "message": "유효하지 않은 전화번호 형식입니다."
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
public @interface SendVerificationCodeApiDocs {
}