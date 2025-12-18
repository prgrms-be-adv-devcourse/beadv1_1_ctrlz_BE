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
		description = "게시글 목록 조회 성공",
		content = @Content(
			examples = @ExampleObject(
				name = "게시글 목록 (2개)",
				value = """
					{
					  "pageNum": 0,
					  "totalPages": 5,
					  "pageSize": 20,
					  "hasNext": true,
					  "contents": [
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
					    },
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
					    }
					  ]
					}
					
					"""
			)
		)
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
