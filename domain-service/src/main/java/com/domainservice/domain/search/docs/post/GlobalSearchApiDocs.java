package com.domainservice.domain.search.docs.post;

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
	summary = "상품 게시글 통합 검색",
	description = """
        Elasticsearch를 활용한 상품 게시글 통합 검색 API입니다.
        
        ### 주요 기능
        - **전문 검색**: 검색어(q)를 통한 제목, 상품명, 설명 전문 검색
        
        - **다중 필터링**: 카테고리, 가격 범위, 태그, 상품 상태, 거래 상태 조합 검색
        
        - **페이징**: `page`, `size` 파라미터 지원
        
        - **다양한 정렬**: 관련도순, 인기순, 가격순, 최신순 지원
        
        ### 검색 예시
        - 검색어만: `?q=아이폰`
        
        - 검색어 + 필터: `?q=맥북&category=전자기기&minPrice=1000000&maxPrice=2000000`
        
        - 필터만: `?category=의류&status=NEW&sort=newest`
        
        - 태그 검색: `?tags=친환경,중고&sort=popular`
        """
)
@ApiResponses({
	@ApiResponse(
		responseCode = "200",
		description = "검색 성공",
		content = @Content(
			examples = {
				@ExampleObject(
					name = "검색어 기반 검색 결과",
					description = "검색어 '아이폰'으로 검색한 결과",
					value = """
						{
						  "pageNum": 0,
						  "totalPages": 3,
						  "pageSize": 24,
						  "hasNext": true,
						  "contents": [
						    {
						      "id": "post-uuid-1234",
						      "title": "아이폰 15 Pro 팝니다",
						      "price": 1200000,
						      "likedCount": 45,
						      "viewCount": 320,
						      "tradeStatus": "SELLING",
						      "primaryImageUrl": "https://s3.bucket/image1.jpg",
						      "updatedAt": "2024-01-15T10:30:00"
						    },
						    {
						      "id": "post-uuid-5678",
						      "title": "아이폰 14 Pro 중고",
						      "price": 850000,
						      "likedCount": 32,
						      "viewCount": 215,
						      "tradeStatus": "SELLING",
						      "primaryImageUrl": "https://s3.bucket/iphone14.jpg",
						      "updatedAt": "2024-01-14T16:20:00"
						    },
						    {
						      "id": "post-uuid-9012",
						      "title": "아이폰 13 미니 판매",
						      "price": 650000,
						      "likedCount": 28,
						      "viewCount": 180,
						      "tradeStatus": "RESERVED",
						      "primaryImageUrl": "https://s3.bucket/iphone13.jpg",
						      "updatedAt": "2024-01-13T09:15:00"
						    }
						  ]
						}
						"""
				),
				@ExampleObject(
					name = "검색 결과 없음",
					description = "검색 조건에 맞는 상품이 없는 경우",
					value = """
						{
						  "pageNum": 0,
						  "totalPages": 0,
						  "pageSize": 24,
						  "hasNext": false,
						  "contents": []
						}
						"""
				)
			}
		)
	),
	@ApiResponse(
		responseCode = "400",
		description = "잘못된 요청",
		content = @Content(
			schema = @Schema(implementation = ErrorResponse.class),
			examples = {
				@ExampleObject(
					name = "유효성 검증 실패",
					value = """
						{
						    "code": 400,
						    "message": "입력값 검증에 실패했습니다.",
						    "errors": [
						        {
						            "field": "minPrice",
						            "rejectedValue": "-1000",
						            "reason": "최소 가격은 0원 이상이어야 합니다."
						        }
						    ]
						}
						"""
				)
			}
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
public @interface GlobalSearchApiDocs {
}