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
	summary = "상품 게시글 단건 조회",
	description = """
		특정 상품 게시글의 상세 정보를 조회합니다.
		
		### 인증 (Header)
		- **`X-REQUEST-ID`**: API Gateway에서 검증된 사용자 UUID
		
		### 권한
		- 비회원 조회 가능
		- header에 userId 존재 시 (로그인)
			- 최근 본 상품 목록에 해당 상품이 추가 됨
			- 해당 게시글이 본인 게시글인지 판단하여 "isMine" 필드로 반환
		
		- 조회 성공 시 해당 게시글의 **조회수가 1 증가**합니다.
		"""
)
@Parameters({

	@Parameter(
		name = "X-REQUEST-ID",
		description = "Gateway를 통해 해더로 넘어온 검증된 사용자 UUID",
		in = ParameterIn.HEADER,
		schema = @Schema(type = "string", defaultValue = "")
	),
	@Parameter(
		name = "postId",
		description = "조회할 게시글의 UUID",
		in = ParameterIn.PATH,
		required = true,
		schema = @Schema(type = "string")
	)
})
@ApiResponses({
	@ApiResponse(
		responseCode = "200",
		description = "게시글 조회 성공"
	),
	@ApiResponse(
		responseCode = "404",
		description = "존재하지 않는 게시글",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(
				value = """
					{
					    "code": 404,
					    "message": "존재하지 않는 게시글입니다."
					}
					"""
			)
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
public @interface GetProductPostApiDocs {
}
