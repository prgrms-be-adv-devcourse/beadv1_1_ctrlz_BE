package com.domainservice.domain.post.post.model.dto.request;

import com.domainservice.domain.post.post.model.enums.ProductStatus;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.List;

/**
 * 상품 게시글 생성 요청 DTO
 */
@Builder
public record ProductPostRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
        String title,

        @NotBlank(message = "상품명은 필수입니다.")
        @Size(max = 255, message = "상품명은 255자를 초과할 수 없습니다.")
        String name,

        @NotNull(message = "가격은 필수입니다.")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Integer price,

        @NotNull(message = "상품 설명은 필수입니다.")
        String description,

        @NotNull(message = "카테고리는 필수입니다.")
        String categoryId,

        @NotNull(message = "상품 상태는 필수입니다.")
        ProductStatus status,

        List<String> tagIds
) {
}