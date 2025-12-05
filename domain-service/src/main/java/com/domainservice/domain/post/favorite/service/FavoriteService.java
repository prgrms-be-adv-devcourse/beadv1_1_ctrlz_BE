package com.domainservice.domain.post.favorite.service;

import static com.common.exception.vo.ProductPostExceptionCode.*;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.model.web.PageResponse;
import com.domainservice.domain.post.favorite.mapper.FavoriteProductMapper;
import com.domainservice.domain.post.favorite.model.dto.FavoritePostResponse;
import com.domainservice.domain.post.favorite.model.dto.FavoriteProductResponse;
import com.domainservice.domain.post.favorite.model.dto.FavoriteStatusResponse;
import com.domainservice.domain.post.favorite.model.entity.FavoriteProduct;
import com.domainservice.domain.post.favorite.repository.FavoriteRepository;
import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.post.post.repository.ProductPostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteService {

	private final FavoriteRepository favoriteRepository;
	private final ProductPostRepository productPostRepository;

	@Transactional
	public FavoritePostResponse addFavoriteProduct(String userId, String productPostId) {

		ProductPost productPost = productPostRepository.findById(productPostId)
			.orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));

		if (favoriteRepository.existsByUserIdAndProductPost(userId, productPost)) {
			throw new ProductPostException(FAVORITE_ALREADY_EXISTS);
		}

		FavoriteProduct favoriteProduct = FavoriteProduct.builder()
			.userId(userId)
			.productPost(productPost)
			.build();

		try {

			favoriteRepository.saveAndFlush(favoriteProduct);
			productPostRepository.incrementLikedCount(productPostId);

			return new FavoritePostResponse(true, productPostId);

		} catch (DataIntegrityViolationException e) {
			throw new ProductPostException(FAVORITE_ALREADY_EXISTS); // DB 유니크 제약조건 위반 (동시 요청)
		}

	}

	@Transactional
	public FavoritePostResponse cancelFavoriteProduct(String userId, String productPostId) {

		FavoriteProduct target = favoriteRepository
			.findByUserIdAndProductPostId(userId, productPostId)
			.orElseThrow(() -> new ProductPostException(FAVORITE_NOT_FOUND));

		favoriteRepository.delete(target);

		productPostRepository.decrementLikedCount(productPostId);

		return new FavoritePostResponse(false, productPostId);

	}

	/**
	 * 내가 찜한 상품 목록 조회
	 */
	@Transactional(readOnly = true)
	public PageResponse<List<FavoriteProductResponse>> getMyFavoriteProductList(String userId, Pageable pageable) {

		Page<FavoriteProduct> page = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

		return new PageResponse<>(
			page.getNumber(),
			page.getTotalPages(),
			page.getSize(),
			page.hasNext(),
			page.getContent().stream()
				.map(FavoriteProduct::getProductPost)
				.map(FavoriteProductMapper::toResponse)
				.toList()
		);

	}

	/**
	 * 찜하기 상태 확인
	 */
	@Transactional(readOnly = true)
	public FavoriteStatusResponse isFavorite(String userId, String productPostId) {
		return new FavoriteStatusResponse(
			favoriteRepository.existsByUserIdAndProductPostId(userId, productPostId)
		);
	}

}