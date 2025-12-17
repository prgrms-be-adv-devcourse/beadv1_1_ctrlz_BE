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
		description = "최근 본 상품 목록 조회 성공",
		content = @Content(
			examples = @ExampleObject(
				name = "게시글 목록 (2개)",
				value = """
					{
					  "data": [
					    {
					      "id": "post-uuid-5678",
					      "userId": "user-uuid-9012",
					      "categoryId": "category-uuid-3456",
					      "title": "맥북 에어 M2 미개봉",
					      "name": "MacBook Air M2 13인치 스타라이트",
					      "price": 1450000,
					      "description": "선물 받았는데 필요 없어서 팝니다. 씰도 안 뜯은 새상품입니다.",
					      "status": "NEW",
					      "tradeStatus": "RESERVED",
					      "imageUrls": [
					        "https://s3.bucket/macbook1.jpg"
					      ],
					      "primaryImageUrl": "https://s3.bucket/macbook1.jpg",
					      "tags": [
					        "노트북",
					        "애플",
					        "맥북"
					      ],
					      "createdAt": "2024-01-16T14:20:00",
					      "updatedAt": "2024-01-16T14:20:00"
					    },
					    {
					      "id": "post-uuid-1234",
					      "userId": "user-uuid-5678",
					      "categoryId": "category-uuid-9012",
					      "title": "아이폰 15 Pro 팝니다",
					      "name": "아이폰 15 Pro 256GB 딥퍼플",
					      "price": 1200000,
					      "description": "작년 12월에 구매한 아이폰입니다. 상태 깨끗합니다.",
					      "status": "NEW",
					      "tradeStatus": "SELLING",
					      "imageUrls": [
					        "https://s3.bucket/image1.jpg",
					        "https://s3.bucket/image2.jpg"
					      ],
					      "primaryImageUrl": "https://s3.bucket/image1.jpg",
					      "tags": [
					        "전자기기",
					        "애플"
					      ],
					      "createdAt": "2024-01-15T10:30:00",
					      "updatedAt": "2024-01-15T10:30:00"
					    }
					  ],
					  "message": "최근 본 상품 목록 조회가 완료되었습니다."
					}
					"""
			)
		)
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

