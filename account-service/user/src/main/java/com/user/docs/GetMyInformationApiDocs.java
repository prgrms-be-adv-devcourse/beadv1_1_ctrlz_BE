package com.user.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.ErrorResponse;
import com.user.infrastructure.reader.port.dto.UserDescription;

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
    summary = "내 정보 조회",
    description = """
        현재 로그인한 사용자의 상세 정보를 조회합니다.
                
        - **반환 형태**: `BaseResponse` 없이 사용자 정보 객체(`UserDescription`)를 직접 반환합니다.
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
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(
            schema = @Schema(implementation = UserDescription.class),
            examples = @ExampleObject(
                value = """
                    {
                        "userId": "user-uuid-1234",
                        "email": "test@example.com",
                        "name": "홍길동",
                        "nickname": "dev_hong",
                        "phoneNumber": "010-1234-5678",
                        "profileUrl": "https://image.url/profile.jpg",
                        "age": 25,
                        "gender": "MALE",
                        "address": {
                            "state": "경기도",
                            "city": "성남시",
                            "street": "분당구 판교역로",
                            "zipCode": "13529",
                            "details": "101동 1202호"
                        },
                        "role": "BUYER"
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
public @interface GetMyInformationApiDocs {
}