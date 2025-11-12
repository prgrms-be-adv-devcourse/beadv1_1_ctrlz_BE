package com.domainservice.domain.post.post.service;

import static com.domainservice.domain.post.post.exception.vo.ProductPostExceptionCode.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.model.persistence.BaseEntity;
import com.common.model.web.PageResponse;
import com.domainservice.domain.asset.image.domain.entity.Image;
import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.mapper.ProductPostMapper;
import com.domainservice.domain.post.post.model.dto.request.CreateProductPostRequest;
import com.domainservice.domain.post.post.model.dto.request.UpdateProductPostRequest;
import com.domainservice.domain.post.post.model.dto.response.ProductPostResponse;
import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.post.post.model.enums.ProductStatus;
import com.domainservice.domain.post.post.model.enums.TradeStatus;
import com.domainservice.domain.post.post.repository.ProductPostRepository;
import com.domainservice.domain.post.tag.model.entity.Tag;
import com.domainservice.domain.post.tag.repository.TagRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductPostService {

	private final ProductPostRepository productPostRepository;
	private final TagRepository tagRepository;

	/**
	 * 상품 게시글 생성 (이미지 포함)
	 */
	public ProductPostResponse createProductPost(
		CreateProductPostRequest request, String userId, List<Image> images) {

		if (images != null && images.size() > 10) {
			throw new ProductPostException(TOO_MANY_IMAGES);
		}

		ProductPost productPost = ProductPost.builder()
			.userId(userId)
			.categoryId(request.categoryId())
			.title(request.title())
			.name(request.name())
			.price(request.price())
			.description(request.description())
			.status(request.status())
			.tradeStatus(TradeStatus.SELLING)
			.build();

		if (images != null && !images.isEmpty()) {
			productPost.addImages(images);
		}

		addTags(productPost, request.tagIds());

		ProductPost saved = productPostRepository.save(productPost);
		return ProductPostMapper.toProductPostResponse(saved);
	}

	/**
	 * 상품 게시글 삭제
	 */
	public String deleteProductPost(String userId, String postId) {

		// TODO: 유저가 실제로 존재하는지 정보 확인

		ProductPost target = productPostRepository.findById(postId)
			.orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));

		target.validateDelete(userId);
		target.delete(); // 상태 변경만을 진행 (SoftDelete)

		return target.getId();
	}

	public ProductPostResponse updateProductPost(String userId, String postId, UpdateProductPostRequest request) {

		ProductPost productPost = productPostRepository.findById(postId)
			.orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));

		productPost.validateUpdate(userId);

		productPost.update(request);
		addTags(productPost, request.tagIds());

		return ProductPostMapper.toProductPostResponse(productPost);
	}

	private void addTags(ProductPost productPost, List<String> tagIds) {
		if (tagIds != null) {
			List<Tag> tags = tagRepository.findAllById(tagIds);

			if (tags.size() != tagIds.size()) {
				throw new ProductPostException(TAG_NOT_FOUND);
			}
			productPost.replaceTags(tags);
		}
	}

	public ProductPostResponse getProductPostById(String postId) {

		ProductPost productPost = productPostRepository.findById(postId)
			.orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));

		// Soft Delete로 삭제된 상품은 상세 조회 불가
		if (productPost.getDeleteStatus() == BaseEntity.DeleteStatus.D) {
			throw new ProductPostException(PRODUCT_POST_DELETED);
		}

		productPost.incrementViewCount();

		return ProductPostMapper.toProductPostResponse(productPost);
	}

	/**
	 * 상품 게시글 목록 조회 (페이징 + 동적 필터링)
	 */
	public PageResponse<List<ProductPostResponse>> getProductPostList(
		Pageable pageable, String categoryId, ProductStatus status,
		TradeStatus tradeStatus, Integer minPrice, Integer maxPrice
	) {

		// Specification 생성
		Specification<ProductPost> spec = ProductPostSpecification.searchWith(
			categoryId, status, tradeStatus, minPrice, maxPrice
		);

		Page<ProductPost> page = productPostRepository.findAll(spec, pageable);

		// PageResponse 생성
		return new PageResponse<>(
			page.getNumber(),
			page.getTotalPages(),
			page.getSize(),
			page.hasNext(),
			page.getContent().stream()
				.map(ProductPostMapper::toProductPostResponse)
				.toList()
		);
	}

	// TODO: 상품 판매상태 변경
	// TODO: 내가 구매한 상품 조회
	// TODO: 내가 판매한 상품 조회
	// TODO: 좋아요 로직 구현
	// TODO: 찜한 게시물
	// TODO: 최근 본 상품 redis?

	public boolean isSellingTradeStatus(String id) {
		return this.getProductPostById(id).tradeStatus() == TradeStatus.SELLING;
	}

	public void updateTradeStatusToProcessing(String postId) {
		ProductPost product = productPostRepository.findById(postId)
			.orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));
		product.markAsProcessing();
		productPostRepository.save(product);
	}

	public void updateTradeStatusToSoldout(String postId) {
		ProductPost product = productPostRepository.findById(postId)
			.orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));
		product.markAsSoldout();
		productPostRepository.save(product);
	}

	public void updateTradeStatusToSelling(String postId) {
		ProductPost product = productPostRepository.findById(postId)
			.orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));
		product.markAsSellingAgain();
		productPostRepository.save(product);
	}
}
