package com.user.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.BaseResponse;
import com.common.model.web.ErrorResponse;
import com.user.infrastructure.api.dto.UpdateSellerRequest;

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
    summary = "판매자 권한 등록 (인증 코드 검증)",
    description = """
        사용자에게 발송된 인증 코드를 검증하고, 성공 시 판매자(SELLER) 권한을 부여합니다.
        
        - **선행 작업**: `/api/users/sellers/verification` (인증 코드 발송) API 호출 필요
        - **동작**: 인증 코드가 일치하고 만료되지 않았을 경우, 사용자 Role을 업데이트합니다.
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
    description = "인증 코드 요청 정보",
    required = true,
    content = @Content(
        schema = @Schema(implementation = UpdateSellerRequest.class),
        examples = @ExampleObject(
            value = """
                {
                    "verificationCode": "123456"
                }
                """
        )
    )
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "인증 성공 및 권한 변경 완료",
        content = @Content(
            schema = @Schema(implementation = BaseResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "data": null,
                        "message": "판매자 등록이 완료됐습니다."
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "인증 실패 (코드 불일치 또는 만료)",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 400,
                        "message": "인증 코드가 일치하지 않거나 만료되었습니다."
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
public @interface UpdateRoleForSellerApiDocs {
}