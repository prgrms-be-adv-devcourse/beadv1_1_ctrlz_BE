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
	summary = "관심 상품 취소",
	description = """
        등록된 관심 상품을 목록에서 제거합니다.
        
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
		description = "관심 상품 등록을 취소할 게시글의 ID",
		required = true,
		in = ParameterIn.PATH
	)
})
@ApiResponses({
	@ApiResponse(
		responseCode = "200",
		description = "관심 상품 취소 성공",
		content = @Content(
			schema = @Schema(implementation = BaseResponse.class),
			examples = @ExampleObject(
				value = """
                    {
                        "data": {
                            "isFavorite": false,
                            "postId": "product-uuid"
                        },
                        "message": "관심 상품 취소에 성공했습니다."
                    }
                    """
			)
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
		description = "좋아요 내역이 없거나 상품이 없는 경우",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(
				value = """
                    {
                        "code": 404,
                        "message": "좋아요하지 않은 글입니다."
                    }
                    """
			)
		)
	)
})
public @interface CancelFavoriteApiDocs {
}
