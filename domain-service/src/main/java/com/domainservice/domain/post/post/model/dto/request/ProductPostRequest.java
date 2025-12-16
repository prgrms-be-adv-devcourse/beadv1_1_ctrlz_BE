package com.domainservice.domain.post.post.model.dto.request;

import java.util.List;

import com.common.model.vo.ProductStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * 상품 게시글 생성 요청 DTO
 */
@Builder
public record ProductPostRequest(
	@Schema(description = "게시글 제목 (최대 200자)", example = "아이폰 15 Pro 팝니다")
	@NotBlank(message = "제목은 필수입니다.")
	@Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
	String title,

	@Schema(description = "상품명 (최대 255자)", example = "아이폰 15 Pro 256GB 딥퍼플")
	@NotBlank(message = "상품명은 필수입니다.")
	@Size(max = 255, message = "상품명은 255자를 초과할 수 없습니다.")
	String name,

	@Schema(description = "가격 (0원 이상)", example = "1200000")
	@NotNull(message = "가격은 필수입니다.")
	@Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
	Integer price,

	@Schema(description = "상품 설명", example = "작년 12월에 구매한 아이폰입니다. 상태 깨끗합니다.")
	@NotNull(message = "상품 설명은 필수입니다.")
	String description,

	@Schema(description = "카테고리 UUID", example = "category-uuid-1234")
	@NotNull(message = "카테고리는 필수입니다.")
	String categoryId,

	@Schema(description = "상품 상태 (NEW: 새상품, GOOD: 중고, FAIR: 사용감 많음)", example = "NEW")
	@NotNull(message = "상품 상태는 필수입니다.")
	ProductStatus status,

	@Schema(description = "태그 ID 목록 (선택)", example = "[\"존재하는 태그 id\", \"존재하는 태그 id\"]")
	List<String> tagIds
) {
}
