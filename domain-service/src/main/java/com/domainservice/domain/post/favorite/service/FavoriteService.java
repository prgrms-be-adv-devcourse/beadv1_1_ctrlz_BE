package com.domainservice.domain.post.favorite.service;

import static com.common.exception.vo.ProductPostExceptionCode.*;

import java.util.List;

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

/**
 * 관심 상품(찜하기) 관련 비즈니스 로직을 처리하는 서비스입니다.
 * 상품 좋아요 등록/취소, 내 관심 목록 조회, 좋아요 여부 확인 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class FavoriteService {

	private final FavoriteRepository favoriteRepository;
	private final ProductPostRepository productPostRepository;

	/**
	 * 관심 상품을 등록합니다.
	 *
	 * @param userId 요청한 사용자 ID
	 * @param productPostId 관심 상품으로 등록할 게시글 ID
	 * @return 등록 성공 여부(true) 및 게시글 ID
	 * @throws ProductPostException 게시글이 없거나 이미 관심 상품으로 등록된 경우
	 */
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

		// db 레벨에서 확실한 중복 등록을 막기 위해 flush를 통해 INSERT 쿼리를 즉시 발송
		favoriteRepository.saveAndFlush(favoriteProduct);

		/*
		 * Dirty Checking 이전에 DB 직접 업데이트를 통해 동시성 문제 완화
		 *
		 * 주의: incrementLikedCount()는 @Modifying을 통해 DB에 직접 UPDATE 쿼리를 날리므로,
		 * 현재 영속성 컨텍스트에 있는 productPost의 likedCount 값과 일치하지 않을 수 있습니다.
		 * (현재 로직에서는 반환값에 조회해왔던 productPost의 likedCount를 사용하지 않아 문제 없습니다.)
		*/
		productPostRepository.incrementLikedCount(productPostId);

		return new FavoritePostResponse(true, productPostId);

	}

	/**
	 * 관심 상품 등록을 취소합니다.
	 * 등록되지 않은 상품인 경우 예외가 발생합니다.
	 *
	 * @param userId 요청한 사용자 ID
	 * @param productPostId 취소할 게시글 ID
	 * @return 취소 성공 여부(false) 및 게시글 ID
	 * @throws ProductPostException 관심 상품 등록 내역이 없는 경우
	 */
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
	 * 내가 찜한 상품 목록을 페이징하여 조회합니다.
	 * 최신 등록순(createdAt Desc)으로 정렬됩니다.
	 *
	 * @param userId 조회할 사용자 ID
	 * @param pageable 페이징 정보
	 * @return 페이징된 관심 상품 목록
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
	 * 특정 상품에 대한 좋아요 여부를 확인합니다.
	 *
	 * @param userId 확인할 사용자 ID
	 * @param productPostId 확인할 게시글 ID
	 * @return 좋아요 여부 (true/false)
	 */
	@Transactional(readOnly = true)
	public FavoriteStatusResponse isFavorite(String userId, String productPostId) {
		return new FavoriteStatusResponse(
			favoriteRepository.existsByUserIdAndProductPostId(userId, productPostId)
		);
	}

}