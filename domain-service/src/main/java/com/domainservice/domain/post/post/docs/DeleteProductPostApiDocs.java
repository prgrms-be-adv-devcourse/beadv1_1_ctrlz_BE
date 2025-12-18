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
	summary = "상품 게시글 삭제",
	description = """
		상품 게시글을 삭제합니다. (Soft Delete)
		
		### 인증 (Header)
		- **`X-REQUEST-ID`**: 로그인을 통해 API Gateway에서 header에 발급된 사용자 UUID (필수)
		
		### 권한
		- 본인이 작성한 게시글만 삭제 가능
		- 해당 userId 회원은 SELLER 또는 ADMIN 권한 필요
		- 거래중(PROCESSING) 상태인 게시글은 삭제 불가
		
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
		description = "삭제할 게시글 ID",
		required = true,
		in = ParameterIn.PATH,
		schema = @Schema(type = "string"),
		example = "post-uuid-1234"
	)
})
@ApiResponses({
	@ApiResponse(
		responseCode = "200",
		description = "상품 게시글 삭제 성공",
		content = @Content(
			examples = @ExampleObject(
				value = """
					{
					    "data": "post-uuid-1234",
					    "message": "상품 게시글이 삭제되었습니다."
					}
					"""
			)
		)
	),
	@ApiResponse(
		responseCode = "401",
		description = "인증되지 않은 사용자 (header에 userId가 없거나 해당 userId 사용자가 존재하지 않음)",
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
		description = "삭제 권한 없음",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = {
				@ExampleObject(
					name = "본인의 글이 아님",
					value = """
						{
						    "code": 403,
						    "message": "자신이 작성한 글만 삭제 가능합니다."
						}
						"""
				),
				@ExampleObject(
					name = "거래중인 글",
					value = """
						{
						    "code": 403,
						    "message": "거래중인 글은 삭제할 수 없습니다."
						}
						"""
				),
				@ExampleObject(
					name = "판매자 권한 없음",
					value = """
						{
						    "code": 403,
						    "message": "판매자 인증이 필요합니다."
						}
						"""
				)
			}
		)
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
					    "message": "해당 글은 존재하지 않습니다."
					}
					"""
			)
		)
	),
	@ApiResponse(
		responseCode = "409",
		description = "이미 삭제된 게시글",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(
				value = """
					{
					    "code": 409,
					    "message": "이미 삭제된 상품입니다."
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
public @interface DeleteProductPostApiDocs {
}