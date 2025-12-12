package com.domainservice.domain.post.post.mapper;

import java.util.List;

import com.domainservice.domain.post.post.model.dto.response.ProductPostDescription;
import com.domainservice.domain.post.post.model.dto.response.ProductPostResponse;
import com.domainservice.domain.post.post.model.entity.ProductPost;

public class ProductPostMapper {

	public static ProductPostResponse toResponse(ProductPost productPost) {

		// 태그 이름 목록
		List<String> tagNames = productPost.getTagNames();

		// 이미지 URL 목록
		List<String> imageUrls = productPost.getAllImageUrls();

		// 대표 이미지 URL
		String primaryImageUrl = productPost.getPrimaryImageUrl();

		return ProductPostResponse.builder()
			.id(productPost.getId())
			.userId(productPost.getUserId())
			.categoryId(productPost.getCategoryId())
			.title(productPost.getTitle())
			.name(productPost.getName())
			.price(productPost.getPrice())
			.description(productPost.getDescription())
			.status(productPost.getStatus())
			.tradeStatus(productPost.getTradeStatus())
			.imageUrls(imageUrls)
			.primaryImageUrl(primaryImageUrl)
			.tags(tagNames)
			.createdAt(productPost.getCreatedAt())
			.updatedAt(productPost.getUpdatedAt())
			.build();

	}

	public static ProductPostDescription toDescription(
		ProductPost productPost, String sellerNickname, String categoryName, String viewerId) {
		// 태그 이름 목록
		List<String> tagNames = productPost.getTagNames();

		// 이미지 URL 목록
		List<String> imageUrls = productPost.getAllImageUrls();

		// 대표 이미지 URL
		String primaryImageUrl = productPost.getPrimaryImageUrl();

		return ProductPostDescription.builder()
			.id(productPost.getId())
			.nickName(sellerNickname)
			.name(productPost.getName())
			.title(productPost.getTitle())
			.description(productPost.getDescription())
			.categoryName(categoryName)
			.tags(tagNames)
			.price(productPost.getPrice())
			.likedCount(productPost.getLikedCount())
			.viewCount(productPost.getViewCount())
			.status(productPost.getStatus())
			.tradeStatus(productPost.getTradeStatus())
			.deleteStatus(productPost.getDeleteStatus())
			.createdAt(productPost.getCreatedAt())
			.updatedAt(productPost.getUpdatedAt())
			.imageUrls(imageUrls)
			.primaryImageUrl(primaryImageUrl)
			.isMine(productPost.getUserId().equals(viewerId))
			.build();

	}

}
