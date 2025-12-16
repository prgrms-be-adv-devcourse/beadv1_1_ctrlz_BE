package com.domainservice.domain.post.post.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Retention(RetentionPolicy.RUNTIME)
@Operation(
	summary = "상품 게시글 목록 조회",
	description = """
        상품 게시글 목록을 페이징하여 조회합니다.
        
        ### 기능
        - **필터링**: 카테고리, 상품 상태, 거래 상태, 가격 범위를 조합하여 검색할 수 있습니다.
        
        - **페이징**: `page`, `size`, `sort` 파라미터를 지원합니다.
        
        - **정렬**:
        	- default: `createdAt,desc` (생성일 기준 내림차순)
        	
        	- 필드명: `createdAt`, `price`, `viewCount`, `likedCount`
        	
        """
)
@ApiResponses({
	@ApiResponse(
		responseCode = "200",
		description = "게시글 목록 조회 성공"
	),
	@ApiResponse(
		responseCode = "400",
		description = "잘못된 요청",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = @ExampleObject(
				name = "유효성 검증 실패",
				value = """
					{
					    "code": 400,
					    "message": "입력값 검증에 실패했습니다.",
					    "errors": [
					        {
					            "field": "price",
					            "rejectedValue": "-1",
					            "reason": "가격은 0원 이상이어야 합니다."
					        },
					        {
					            "field": "name",
					            "rejectedValue": "",
					            "reason": "상품명은 필수입니다."
					        }
					    ]
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
public @interface GetProductPostListApiDocs {
}
