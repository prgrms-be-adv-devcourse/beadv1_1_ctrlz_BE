package com.domainservice.domain.post.favorite.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.ErrorResponse;
import com.common.model.web.PageResponse;

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
	summary = "내 관심 상품 목록 조회",
	description = """
        사용자가 등록한 관심 상품(좋아요) 목록을 페이징 처리하여 조회합니다.
        
        기본 정렬은 최신순(`createdAt DESC`)입니다.
        
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
		name = "page",
		description = "페이지 번호 (0부터 시작)",
		in = ParameterIn.QUERY,
		schema = @Schema(type = "integer", defaultValue = "0")
	),
	@Parameter(
		name = "size",
		description = "한 페이지당 조회할 개수",
		in = ParameterIn.QUERY,
		schema = @Schema(type = "integer", defaultValue = "20")
	),
	@Parameter(
		name = "sort",
		in = ParameterIn.QUERY,
		description = """
			정렬 기준을 지정합니다.
			
			**형식**: `필드명,정렬방향`
			
			**사용 가능한 필드명**:
			- `createdAt`: 등록일 (최신순/오래된순)
			- `price`: 가격 (높은순/낮은순)
			- `viewCount`: 조회수
			- `likedCount`: 좋아요 수 (인기순)
			
			**정렬 방향**:
			- `desc`: 내림차순
			- `asc`: 오름차순
			
			**주요 예시**:
			- 최신순: `createdAt,desc` (기본값)
			- 가격 낮은순: `price,asc`
			- 인기순: `likedCount,desc`
        """,
		example = "createdAt,desc",
		schema = @Schema(
			type = "string",
			defaultValue = "createdAt,desc"
		)
	)
})
@ApiResponses({
	@ApiResponse(
		responseCode = "200",
		description = "조회 성공 (페이징)",
		content = @Content(
			schema = @Schema(implementation = PageResponse.class),
			examples = @ExampleObject(
				value = """
                    {
                        "pageNum": 0,
                        "totalPages": 5,
                        "pageSize": 20,
                        "hasNext": true,
                        "contents": [
                            {
                                "productPostId": "product-uuid-1",
                                "title": "나이키 에어포스 1 (새상품)",
                                "price": 120000,
                                "tradeStatus": "SELLING",
                                "primaryImageUrl": "https://example.com/image1.jpg",
                                "createdAt": "2025-12-13T16:09:29"
                            },
                            {
                                "productPostId": "product-uuid-2",
                                "title": "다이슨 무선 청소기",
                                "price": 390000,
                                "tradeStatus": "PROCESSING",
                                "primaryImageUrl": "https://example.com/image2.jpg",
                                "createdAt": "2025-12-12T10:00:00"
                            }
                        ]
                    }
                    """
			)
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
public @interface GetMyFavoriteListApiDocs {
}
