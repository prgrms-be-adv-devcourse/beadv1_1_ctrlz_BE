package com.domainservice.domain.review.docs;

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
	summary = "리뷰 생성",
	description = """
		상품(게시글)에 대한 리뷰를 작성합니다.
	
		### 인증 (Header)
		- **`X-REQUEST-ID`**: 로그인을 통해 API Gateway에서 header에 발급된 사용자 UUID (필수)
	
		### 제약 사항
		- 해당 상품을 **구매한 기록이 있는 사용자**만 작성할 수 있습니다.
		"""
)
@Parameters({
	@Parameter(
		name = "X-REQUEST-ID",
		description = "사용자 ID (구매자)",
		in = ParameterIn.HEADER,
		schema = @Schema(type = "string", example = "user-uuid-1234")
	)
})
@ApiResponses({
	@ApiResponse(
		responseCode = "201",
		description = "리뷰 작성 성공"
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
					    "message": "유효하지 않은 상품 평점입니다."
					}
					"""
			)
		)
	),
	@ApiResponse(
		responseCode = "403",
		description = "권한 없음 (구매 내역 없음)",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(
				name = "구매 기록 없음",
				value = """
					{
					    "code": 403,
					    "message": "해당 상품에 대한 구매 기록이 없습니다."
					}
					"""
			)
		)
	),
	@ApiResponse(
		responseCode = "404",
		description = "존재하지 않는 상품",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(
				value = """
					{
					    "code": 404,
					    "message": "존재하지 않는 상품 게시글입니다."
					}
					"""
			)
		)
	),
	@ApiResponse(
		responseCode = "409",
		description = "이미 작성된 리뷰 존재",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(
				value = """
					{
					    "code": 409,
					    "message": "이미 해당 상품에 대한 리뷰를 작성했습니다."
					}
					"""
			)
		)
	)
})
public @interface CreateReviewApiDocs {
}
