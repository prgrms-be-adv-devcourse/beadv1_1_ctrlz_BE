package com.user.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.ErrorResponse;
import com.user.infrastructure.reader.port.dto.UserDemographicDescription;

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
    summary = "추천용 회원 정보 조회",
    description = """
        추천 시스템 등에서 필요한 회원의 인구통계학적 정보(나이, 성별 등)를 조회합니다.
        
        - **반환 형태**: `BaseResponse` 없이 `UserDemographicDescription` 객체를 직접 반환합니다.
        """
)
@Parameters({
    @Parameter(
        name = "userId",
        description = "조회할 회원 ID",
        required = true,
        in = ParameterIn.PATH
    )
})
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(
            schema = @Schema(implementation = UserDemographicDescription.class),
            examples = @ExampleObject(
                value = """
                    {
                        "userId": "user-uuid-1234",
                        "age": 25,
                        "gender": "MALE",
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
public @interface GetRecommendationInfoApiDocs {
}