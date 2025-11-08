package com.domainservice.domain.post.post.mapper;

import com.domainservice.domain.post.post.model.dto.response.ProductPostResponse;
import com.domainservice.domain.post.post.model.entity.ProductPost;

import java.util.List;

public class ProductPostMapper {

    public static ProductPostResponse toProductPostResponse(ProductPost productPost) {

        // 태그 이름 목록
        List<String> tagNames = productPost.getProductPostTags().stream()
                .map(pt -> pt.getTag().getName())
                .toList();

        // 이미지 URL 목록
        List<String> imageUrls = productPost.getAllImageUrls();

        // 대표 이미지 URL
        String primaryImageUrl = productPost.getPrimaryImageUrl();

        return new ProductPostResponse(
                productPost.getId(),
                productPost.getUserId(),
                productPost.getCategoryId(),
                productPost.getTitle(),
                productPost.getName(),
                productPost.getPrice(),
                productPost.getDescription(),
                productPost.getStatus(),
                productPost.getTradeStatus(),
                imageUrls,
                primaryImageUrl,
                tagNames,
                productPost.getCreatedAt(),
                productPost.getUpdatedAt()
        );

    }

}
