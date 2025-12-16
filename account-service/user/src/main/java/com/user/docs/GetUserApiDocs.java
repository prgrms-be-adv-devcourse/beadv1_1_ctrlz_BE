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
    summary = "회원 정보 조회 (특정 ID)",
    description = """
        특정 회원(ID)의 상세 정보를 조회합니다.
        다른 사용자의 프로필을 조회하거나 관리자가 회원을 조회할 때 사용됩니다.
        
        - **반환 형태**: `BaseResponse` 없이 `UserDescription` 객체를 직접 반환합니다.
        """
)
@Parameters({
    @Parameter(
        name = "id",
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
            schema = @Schema(implementation = UserDescription.class),
            examples = @ExampleObject(
                value = """
                    {
                        "userId": "user-uuid-9876",
                        "email": "other@example.com",
                        "name": "김철수",
                        "nickname": "cheolsu_zzang",
                        "phoneNumber": "010-5678-1234",
                        "profileUrl": "https://image.url/profile_2.jpg",
                        "age": 30,
                        "gender": "FEMALE",
                        "address": {
                            "state": "서울특별시",
                            "city": "서초구",
                            "street": "서초대로",
                            "zipCode": "06611",
                            "details": "202호"
                        },
                        "role": "SELLER"
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
public @interface GetUserApiDocs {
}