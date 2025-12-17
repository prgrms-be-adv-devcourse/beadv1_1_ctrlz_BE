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
	summary = "오늘의 추천 상품",
	description = """
        최근 3일(72시간) 내 등록된 상품 중 인기 상품을 추천하는 API입니다.
        
        ### 주요 기능
        - **시간 기반 필터**: 최근 72시간 내 등록된 신규 상품만 조회
        
        - **인기도 기반 정렬**: Function Score를 사용한 가중치 기반 인기도 계산
        
        - **카테고리 필터**: 특정 카테고리의 인기 상품 조회 가능 (선택)
        
        - **페이징**: `page`, `size` 파라미터 지원 (기본 12개)
        
        ### 인기도 계산 방식
        - **좋아요 점수**: `sqrt(liked_count) × 5.0`
        
        - **조회수 점수**: `log(view_count + 1) × 3.0`
        
        - **최종 점수**: 좋아요 점수 + 조회수 점수
        
        - 좋아요에 더 높은 가중치를 부여하여 사용자 관심도를 우선 반영
        
        ### 사용 예시
        - 메인 페이지 "오늘의 인기 상품" 섹션
        
        - 메인 페이지 "OO 카테고리 인기 상품" 섹션
        """,
	parameters = {
		@Parameter(
			name = "category",
			description = "카테고리명 (기본값: all)",
			example = "전자기기",
			schema = @Schema(defaultValue = "all")
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
					name = "전체 카테고리 인기 상품",
					description = "최근 3일간 등록된 모든 카테고리의 인기 상품",
					value = """
						{
						  "pageNum": 0,
						  "totalPages": 3,
						  "pageSize": 12,
						  "hasNext": true,
						  "contents": [
						    {
						      "id": "post-uuid-1234",
						      "title": "아이폰 15 Pro 새상품",
						      "price": 1350000,
						      "likedCount": 89,
						      "viewCount": 1250,
						      "tradeStatus": "SELLING",
						      "primaryImageUrl": "https://s3.bucket/iphone15.jpg",
						      "updatedAt": "2024-01-17T10:30:00"
						    },
						    {
						      "id": "post-uuid-5678",
						      "title": "맥북 에어 M3 미개봉",
						      "price": 1580000,
						      "likedCount": 76,
						      "viewCount": 980,
						      "tradeStatus": "SELLING",
						      "primaryImageUrl": "https://s3.bucket/macbook.jpg",
						      "updatedAt": "2024-01-16T14:20:00"
						    },
						    {
						      "id": "post-uuid-9012",
						      "title": "다이슨 청소기 V15 새상품",
						      "price": 680000,
						      "likedCount": 62,
						      "viewCount": 850,
						      "tradeStatus": "SELLING",
						      "primaryImageUrl": "https://s3.bucket/dyson.jpg",
						      "updatedAt": "2024-01-16T09:15:00"
						    }
						  ]
						}
						"""
				),
				@ExampleObject(
					name = "특정 카테고리 인기 상품",
					description = "모바일/태블릿 카테고리의 인기 상품",
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
					name = "입력한 카테고리가 존재하지 않음",
					description = "입력된 카테고리가 존재하지 않는 경우",
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
public @interface GetDailyRecommendationApiDocs {
}