package com.domainservice.domain.post.post.model.dto.request;

import com.domainservice.domain.post.post.model.enums.ProductStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 상품 게시글 수정 요청 DTO
 */
public record UpdateProductPostRequest(
        @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
        String title,

        @Size(max = 255, message = "상품명은 255자를 초과할 수 없습니다.")
        String name,

        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Integer price,

        String description,

        ProductStatus status,

        String imageUrl,

        List<String> tagIds
) {
}