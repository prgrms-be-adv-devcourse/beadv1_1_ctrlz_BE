package com.domainservice.domain.reivew.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
	summary = "리뷰 수정",
	description = """
        작성한 리뷰를 수정합니다.
        
        ### 제약 사항
        - **본인이 작성한 리뷰**만 수정할 수 있습니다.
        """
)
@Parameters({
	@Parameter(
		name = "reviewId",
		description = "수정할 리뷰의 UUID",
		in = ParameterIn.PATH,
		required = true,
		schema = @Schema(type = "string")
	),
	@Parameter(
		name = "X-REQUEST-ID",
		description = "사용자 ID (작성자)",
		in = ParameterIn.HEADER,
		schema = @Schema(type = "string", example = "user-uuid-1234")
	)
})
@ApiResponses({
	@ApiResponse(
		responseCode = "200",
		description = "리뷰 수정 성공"
	),
	@ApiResponse(
		responseCode = "400",
		description = "잘못된 요청 (평점 범위 오류 등)",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(
				value = """
                {
                    "code": 400,
                    "message": "내용은 필수 입력값입니다."
                }
                """
			)
		)
	),
	@ApiResponse(
		responseCode = "403",
		description = "권한 없음 (작성자가 아님)",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(
				value = """
                {
                    "code": 403,
                    "message": "해당 리뷰를 수정할 권한이 없습니다."
                }
                """
			)
		)
	),
	@ApiResponse(
		responseCode = "404",
		description = "존재하지 않는 리뷰",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(
				value = """
                {
                    "code": 404,
                    "message": "존재하지 않는 리뷰입니다."
                }
                """
			)
		)
	)
})
public @interface UpdateReviewApiDocs {
}
