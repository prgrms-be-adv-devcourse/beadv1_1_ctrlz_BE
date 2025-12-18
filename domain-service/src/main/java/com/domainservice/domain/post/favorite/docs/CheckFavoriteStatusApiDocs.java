package com.domainservice.domain.post.favorite.docs;

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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Retention(RetentionPolicy.RUNTIME)
@Operation(
	summary = "관심 상품 등록 여부 조회",
	description = """
        특정 상품에 대해 사용자가 '좋아요(관심 상품)'를 눌렀는지 상태를 확인합니다.
        
        ### 인증 (Header)
        - **`X-REQUEST-ID`**: API Gateway에서 검증된 사용자 UUID (필수)
        """
)
@Parameters({
	@Parameter(
		name = "X-REQUEST-ID",
		description = "사용자 ID",
		in = ParameterIn.HEADER,
		schema = @Schema(type = "string", defaultValue = "")
	),
	@Parameter(
		name = "productPostId",
		description = "좋아요 여부를 확인할 게시글 ID",
		required = true,
		in = ParameterIn.PATH
	)
})
@ApiResponses({
	@ApiResponse(
		responseCode = "200",
		description = "조회 성공",
		content = @Content(
			schema = @Schema(implementation = BaseResponse.class),
			examples = {
				@ExampleObject(
					name = "등록된 상태 (True)",
					summary = "좋아요를 누른 상태일 때",
					value = """
                        {
                            "data": {
                                "isFavorite": true
                            },
                            "message": "해당 상품에 대한 좋아요 여부 조회를 성공했습니다."
                        }
                        """
				),
				@ExampleObject(
					name = "미등록 상태 (False)",
					summary = "좋아요를 누르지 않은 상태일 때",
					value = """
                        {
                            "data": {
                                "isFavorite": false
                            },
                            "message": "해당 상품에 대한 좋아요 여부 조회를 성공했습니다."
                        }
                        """
				)
			}
		)
	),
	@ApiResponse(
		responseCode = "401",
		description = "로그인하지 않은 경우",
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
	)
})
public @interface CheckFavoriteStatusApiDocs {
}
