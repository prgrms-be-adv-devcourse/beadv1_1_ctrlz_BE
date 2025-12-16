package com.domainservice.domain.post.post.docs;

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
	summary = "상품 게시글 생성",
	description = """
		새로운 상품 게시글을 생성합니다.
		
		### 인증 (Header)
		- **`X-REQUEST-ID`**: API Gateway에서 검증된 사용자 UUID (필수)
		
		### 권한
		- 해당 userId 회원은 SELLER 또는 ADMIN 권한 필요
		
		### 요청 형식
		- Content-Type: multipart/form-data
		
		- **images**: 상품 이미지 파일 (최소 1개, 최대 10개)
		
		- **request**: 상품 정보 JSON 데이터
			- **categoryId**: 존재하는 카테고리 ID (필수)
			- **title**: 게시글 제목 (필수, 최대 200자)
			- **name**: 상품명 (필수, 최대 255자)
			- **price**: 가격 (필수, 0원 이상)
			- **description**: 상품 설명 (필수)
			- **status**: 상품 상태 (필수)
			  - `NEW`: 새상품
			  - `GOOD`: 중고
			  - `FAIR`: 사용감많음
			- **tagIds**: 태그 ID 목록 (선택, 배열)
		
		"""
)
@Parameters({
	@Parameter(
		name = "X-REQUEST-ID",
		description = "Gateway를 통해 해더로 넘어온 검증된 사용자 UUID",
		in = ParameterIn.HEADER,
		schema = @Schema(type = "string", defaultValue = "")
	)
})
@ApiResponses({
	@ApiResponse(
		responseCode = "201",
		description = "상품 게시글 생성 성공"
	),
	@ApiResponse(
		responseCode = "400",
		description = "잘못된 요청 (이미지 없음 또는 10개 초과, 유효성 검증 실패)",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = {
				@ExampleObject(
					name = "이미지 없음",
					value = """
						{
						    "code": 400,
						    "message": "이미지는 최소 1개 이상 첨부해야 합니다."
						}
						"""
				),
				@ExampleObject(
					name = "이미지 개수 초과",
					value = """
						{
						    "code": 400,
						    "message": "이미지는 최대 10개까지 등록 가능합니다."
						}
						"""
				)
			}
		)
	),
	@ApiResponse(
		responseCode = "401",
		description = "인증되지 않은 사용자",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(
				value = """
					{
					    "code": 401,
					    "message": "로그인된 회원만 이용 가능합니다."
					}
					"""
			)
		)
	),
	@ApiResponse(
		responseCode = "403",
		description = "판매자 권한 없음",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(
				value = """
					{
					    "code": 403,
					    "message": "판매자 인증이 필요합니다."
					}
					"""
			)
		)
	),
	@ApiResponse(
		responseCode = "404",
		description = "존재하지 않는 카테고리 또는 태그",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = {
				@ExampleObject(
					name = "카테고리 없음",
					value = """
						{
						    "code": 404,
						    "message": "존재하지 않는 카테고리입니다."
						}
						"""
				),
				@ExampleObject(
					name = "태그 없음",
					value = """
						{
						    "code": 404,
						    "message": "존재하지 않는 태그가 포함되어있습니다."
						}
						"""
				)
			}
		)
	),
	@ApiResponse(
		responseCode = "500",
		description = "서버 내부 오류",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(
				value = """
					{
					    "code": 500,
					    "message": "서버 내부 오류가 발생했습니다."
					}
					"""
			)
		)
	)
})
public @interface CreateProductPostApiDocs {
}
