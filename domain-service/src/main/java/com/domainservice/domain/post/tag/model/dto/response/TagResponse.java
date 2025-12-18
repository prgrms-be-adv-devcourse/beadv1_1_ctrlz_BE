package com.domainservice.domain.post.tag.model.dto.response;

import com.domainservice.domain.post.tag.model.entity.Tag;
import io.swagger.v3.oas.annotations.media.Schema;

public record TagResponse(
	@Schema(description = "태그 ID", example = "tag-uuid-1")
	String id,

	@Schema(description = "태그 이름", example = "전자기기")
	String name
) {
	/**
	 * Entity -> DTO 변환
	 */
	public static TagResponse from(Tag tag) {
		return new TagResponse(
			tag.getId(),
			tag.getName()
		);
	}
}
