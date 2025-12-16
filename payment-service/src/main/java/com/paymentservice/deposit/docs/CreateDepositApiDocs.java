package com.paymentservice.deposit.docs;

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
    summary = "예치금 계좌 생성",
    description = """
        사용자의 예치금(포인트/지갑) 계좌를 최초 생성합니다.
        
        - **초기 잔액**: 0원으로 생성됩니다.
        - **주의**: 이미 예치금 계좌가 존재하는 경우 에러가 발생합니다.
        
        ### 인증 (Header)
        - **`X-REQUEST-ID`**: API Gateway에서 검증된 사용자 UUID (필수)
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
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "계좌 생성 성공",
        content = @Content(
            schema = @Schema(implementation = BaseResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "data": {
                            "depositId": "deposit-uuid-111",
                            "balance": 0,
                            "message": "success"
                        },
                        "message": "예치금 생성 성공"
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "이미 계좌가 존재하는 경우",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 400,
                        "message": "이미 예치금 계좌가 존재합니다."
                    }
                    """
            )
        )
    )
})
public @interface CreateDepositApiDocs {
}