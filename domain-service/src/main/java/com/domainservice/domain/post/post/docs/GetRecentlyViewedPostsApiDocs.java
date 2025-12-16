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
	summary = "최근 본 상품 목록 조회",
	description = """
        사용자가 최근에 조회한 상품 게시글 목록을 반환합니다.
        
        ### 기능
        - **최대 개수**: Redis 설정에 따라 최근 본 상품 N개(최대 10개)를 반환합니다.
        - **정렬**: 최근에 본 순서대로 정렬되어 반환됩니다.
        
		### 인증 (Header)
		- **`X-REQUEST-ID`**: 로그인을 통해 API Gateway에서 header에 발급된 사용자 UUID (필수)
        """
)
@Parameters({
	@Parameter(
		name = "X-REQUEST-ID",
		description = "사용자 ID (필수)",
		in = ParameterIn.HEADER,
		required = true,
		schema = @Schema(type = "string", example = "user-uuid-1234")
	)
})
@ApiResponses({
	@ApiResponse(
		responseCode = "200",
		description = "최근 본 상품 목록 조회 성공"
	),
	@ApiResponse(
		responseCode = "401",
		description = "인증되지 않은 사용자 (헤더 누락 또는 조회되지 않는 사용자)",
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
public @interface GetRecentlyViewedPostsApiDocs {
}

