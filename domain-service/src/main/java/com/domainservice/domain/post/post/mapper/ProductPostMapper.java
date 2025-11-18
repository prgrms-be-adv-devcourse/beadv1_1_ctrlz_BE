package com.domainservice.domain.post.post.mapper;

import java.util.List;

import com.domainservice.domain.post.post.model.dto.response.ProductPostResponse;
import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.search.model.entity.dto.document.ProductPostDocumentEntity;
import com.domainservice.domain.search.model.entity.dto.response.ProductPostSearchResponse;

public class ProductPostMapper {

	// Elasticsearch Document를 검색 응답 DTO로 변환
	public static ProductPostSearchResponse toProductPostSearchResponse(ProductPostDocumentEntity document) {
		return ProductPostSearchResponse.builder()
			.id(document.getId())
			.name(document.getName())
			.title(document.getTitle())
			.description(document.getDescription())
			.tags(document.getTags())
			.categoryName(document.getCategoryName())
			.price(document.getPrice())
			.likedCount(document.getLikedCount())
			.viewCount(document.getViewCount())
			.status(document.getStatus())
			.tradeStatus(document.getTradeStatus())
			.deleteStatus(document.getDeleteStatus())
			.createdAt(document.getCreatedAt())
			.build();
	}

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
