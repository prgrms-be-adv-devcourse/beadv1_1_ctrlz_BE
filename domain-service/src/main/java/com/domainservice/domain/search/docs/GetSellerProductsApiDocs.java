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
	summary = "판매자의 다른 상품 추천",
	description = """
        같은 판매자가 판매 중인 다른 상품을 추천하는 API입니다.
        
        ### 주요 기능
        - **판매자 기반 추천**: 기준 상품의 판매자가 올린 다른 상품들을 조회합니다.
        
        - **자동 필터링**: 기준 상품 자체와 판매 완료된 상품(SOLDOUT)은 자동으로 제외됩니다.
        
        - **페이징**: `page`, `size` 파라미터 지원 (기본 12개)
        
        - 최신 등록순으로 정렬됩니다.
        
        ### 사용 예시
        - 상품 상세 페이지에서 "판매자의 다른 상품" 섹션
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
					name = "판매자의 다른 상품 목록",
					description = "같은 판매자가 올린 다른 상품들",
					value = """
						{
						  "pageNum": 0,
						  "totalPages": 1,
						  "pageSize": 12,
						  "hasNext": false,
						  "contents": [
						    {
						      "id": "post-uuid-5678",
						      "title": "갤럭시 버즈 프로 새상품",
						      "price": 180000,
						      "likedCount": 28,
						      "viewCount": 150,
						      "tradeStatus": "SELLING",
						      "primaryImageUrl": "https://s3.bucket/buds.jpg",
						      "updatedAt": "2024-01-14T16:20:00"
						    },
						    {
						      "id": "post-uuid-9012",
						      "title": "에어팟 프로 2세대",
						      "price": 280000,
						      "likedCount": 35,
						      "viewCount": 220,
						      "tradeStatus": "SELLING",
						      "primaryImageUrl": "https://s3.bucket/airpods.jpg",
						      "updatedAt": "2024-01-12T11:45:00"
						    },
						    {
						      "id": "post-uuid-3456",
						      "title": "애플워치 SE 2세대",
						      "price": 320000,
						      "likedCount": 42,
						      "viewCount": 280,
						      "tradeStatus": "RESERVED",
						      "primaryImageUrl": "https://s3.bucket/watch.jpg",
						      "updatedAt": "2024-01-10T09:30:00"
						    }
						  ]
						}
						"""
				),
				@ExampleObject(
					name = "다른 상품 없음",
					description = "판매자의 다른 판매 중인 상품이 없는 경우",
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
public @interface GetSellerProductsApiDocs {
}