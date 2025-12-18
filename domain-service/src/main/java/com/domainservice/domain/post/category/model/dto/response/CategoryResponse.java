package com.domainservice.domain.post.category.model.dto.response;

import com.domainservice.domain.post.category.model.entity.Category;

import io.swagger.v3.oas.annotations.media.Schema;

public record CategoryResponse(
	@Schema(description = "카테고리 ID", example = "category-uuid-1")
	String id,

	@Schema(description = "카테고리 이름", example = "가전제품")
	String name
) {
	/**
	 * Entity -> DTO 변환
	 */
	public static CategoryResponse from(Category category) {
		return new CategoryResponse(
			category.getId(),
			category.getName()
		);
	}
}