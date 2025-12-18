package com.domainservice.domain.post.post.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.common.model.vo.ProductStatus;
import com.common.model.vo.TradeStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 상품 게시글 응답 DTO
 */
@Builder
public record ProductPostResponse(
	@Schema(description = "게시글 ID", example = "post-uuid-1234")
	String id,

	@Schema(description = "작성자 ID", example = "user-uuid-5678")
	String userId,

	@Schema(description = "카테고리 ID", example = "category-uuid-9012")
	String categoryId,

	@Schema(description = "게시글 제목", example = "아이폰 15 Pro 팝니다")
	String title,

	@Schema(description = "상품명", example = "아이폰 15 Pro 256GB 딥퍼플")
	String name,

	@Schema(description = "가격", example = "1200000")
	Integer price,

	@Schema(description = "상품 설명", example = "작년 12월에 구매한 아이폰입니다. 상태 깨끗합니다.")
	String description,

	@Schema(description = "상품 상태", example = "NEW")
	ProductStatus status,

	@Schema(description = "거래 상태", example = "SELLING")
	TradeStatus tradeStatus,

	@Schema(description = "업로드된 이미지 URL 목록", example = "[\"https://s3.bucket/image1.jpg\", \"https://s3.bucket/image2.jpg\"]")
	List<String> imageUrls,

	@Schema(description = "대표 이미지 URL", example = "https://s3.bucket/image1.jpg")
	String primaryImageUrl,

	@Schema(description = "태그 목록", example = "[\"전자기기\", \"애플\"]")
	List<String> tags,

	@Schema(description = "생성일시", example = "2024-01-15T10:30:00")
	LocalDateTime createdAt,

	@Schema(description = "수정일시", example = "2024-01-15T10:30:00")
	LocalDateTime updatedAt
) {
}
