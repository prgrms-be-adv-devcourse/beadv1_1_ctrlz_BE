package com.domainservice.domain.search.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.common.model.web.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Retention(RetentionPolicy.RUNTIME)
@Operation(
	summary = "비슷한 상품 추천",
	description = """
        Elasticsearch의 More Like This 쿼리를 활용한 유사 상품 추천 API입니다.
        
        ### 주요 기능
        - **유사도 기반 추천**: 제목, 상품명, 설명, 태그를 기반으로 유사한 상품을 찾아냅니다.
        
        - **자동 필터링**: 현재 판매중인 상품(SELLING)만 자동으로 필터링됩니다.
        
        - **페이징**: `page`, `size` 파라미터 지원 (기본 12개)
        
        - 유사도 점수가 높은 순서로 정렬됩니다.
        
        ### 사용 예시
        - 상품 상세 페이지에서 "비슷한 상품" 섹션
        """,
	parameters = {
		@Parameter(
			name = "productPostId",
			description = "기준이 되는 상품 게시글 ID",
			required = true,
			example = "post-uuid-1234"
		)
	}
)
@ApiResponses({
	@ApiResponse(
		responseCode = "200",
		description = "추천 성공",
		content = @Content(
			examples = {
				@ExampleObject(
					name = "유사 상품 목록",
					description = "아이폰 15 Pro와 유사한 상품 추천 결과",
					value = """
						{
						  "pageNum": 0,
						  "totalPages": 2,
						  "pageSize": 12,
						  "hasNext": true,
						  "contents": [
						    {
						      "id": "post-uuid-5678",
						      "title": "아이폰 15 Pro Max 팝니다",
						      "price": 1450000,
						      "likedCount": 52,
						      "viewCount": 380,
						      "tradeStatus": "SELLING",
						      "primaryImageUrl": "https://s3.bucket/iphone15max.jpg",
						      "updatedAt": "2024-01-16T14:20:00"
						    },
						    {
						      "id": "post-uuid-9012",
						      "title": "아이폰 14 Pro 256GB",
						      "price": 980000,
						      "likedCount": 38,
						      "viewCount": 290,
						      "tradeStatus": "SELLING",
						      "primaryImageUrl": "https://s3.bucket/iphone14pro.jpg",
						      "updatedAt": "2024-01-15T11:45:00"
						    },
						    {
						      "id": "post-uuid-3456",
						      "title": "아이폰 15 일반 미개봉",
						      "price": 1050000,
						      "likedCount": 45,
						      "viewCount": 315,
						      "tradeStatus": "SELLING",
						      "primaryImageUrl": "https://s3.bucket/iphone15.jpg",
						      "updatedAt": "2024-01-14T09:30:00"
						    }
						  ]
						}
						"""
				),
				@ExampleObject(
					name = "추천 결과 없음",
					description = "유사한 상품이 없는 경우",
					value = """
						{
						  "pageNum": 0,
						  "totalPages": 0,
						  "pageSize": 12,
						  "hasNext": false,
						  "contents": []
						}
						"""
				)
			}
		)
	),
	@ApiResponse(
		responseCode = "404",
		description = "상품을 찾을 수 없음",
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
public @interface GetSimilarProductsApiDocs {
}