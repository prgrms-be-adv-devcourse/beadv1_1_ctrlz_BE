package com.user.docs;

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
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "프로필 이미지 수정",
    description = """
        사용자의 프로필 이미지를 변경합니다.
        
        - **Content-Type**: `multipart/form-data`
        - **기능**: 이미지 파일을 업로드하여 기존 프로필을 교체합니다.
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
    ),
    @Parameter(
        name = "imageId",
        description = "수정할 기존 이미지 ID",
        required = true,
        in = ParameterIn.PATH
    )
})
@RequestBody(
    description = "업로드할 이미지 파일",
    required = true,
    content = @Content(
        mediaType = "multipart/form-data",
        schema = @Schema(implementation = UpdateProfileImageApiDocs.ProfileImageUploadRequest.class)
    )
)
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "이미지 업로드 성공",
        content = @Content(
            schema = @Schema(implementation = BaseResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "data": "https://s3.ap-northeast-2.amazonaws.com/bucket/profile/new-image.jpg",
                        "message": "프로필 이미지 교체 완료"
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "파일 누락 또는 형식 오류",
        content = @Content(
            schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 400,
                        "message": "이미지 파일이 존재하지 않습니다."
                    }
                    """
            )
        )
    )
})
public @interface UpdateProfileImageApiDocs {
    // MultipartFile 인터페이스 대신 Swagger 문서용으로 사용할 가짜 클래스
    // 이 클래스가 있어야 Swagger UI에 '파일 선택' 버튼이 나타남.
    class ProfileImageUploadRequest {
        @Schema(
            description = "업로드할 이미지 파일 (jpg, png 등)",
            type = "string",
            format = "binary"
        )
        public Object profileImage;
    }
}