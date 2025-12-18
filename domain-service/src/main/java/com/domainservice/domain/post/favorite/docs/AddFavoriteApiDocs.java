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
	summary = "관심 상품 등록",
	description = """
        특정 상품을 관심 상품(좋아요) 목록에 추가합니다.
        
        ### 인증 (Header)
        - **`X-REQUEST-ID`**: API Gateway에서 검증된 사용자 UUID (필수)
        """
)
@Parameters({
	@Parameter(
		name = "X-REQUEST-ID",
		description = "사용자 ID",
		in = ParameterIn.HEADER,
		schema = @Schema(type = "string", defaultValue = "") // defaultValue 노출 안되게 하려고 사용
	),
	@Parameter(
		name = "productPostId",
		description = "관심 상품으로 등록할 게시글의 ID",
		required = true,
		in = ParameterIn.PATH
	)
})
@ApiResponses({
	@ApiResponse(
		responseCode = "201",
		description = "관심 상품 등록 성공",
		content = @Content(
			schema = @Schema(implementation = BaseResponse.class),
			examples = @ExampleObject(
				value = """
                    {
                        "data": {
                            "isFavorite": true,
                            "postId": "product-uuid"
                        },
                        "message": "관심 상품 등록에 성공했습니다."
                    }
                    """
			)
		)
	),
	@ApiResponse(
		responseCode = "400",
		description = "이미 좋아요를 누른 경우",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = {
				@ExampleObject(
					value = """
                        {
                            "code": 400,
                            "message": "이미 좋아요한 글입니다."
                        }
                        """
				)
			}
		)
	),
	@ApiResponse(
		responseCode = "401",
		description = "로그인하지 않은 경우 (header에 유저 Id X)",
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
		responseCode = "404",
		description = "존재하지 않는 상품에 좋아요를 요청한 경우",
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
	)
})
public @interface AddFavoriteApiDocs {
}
