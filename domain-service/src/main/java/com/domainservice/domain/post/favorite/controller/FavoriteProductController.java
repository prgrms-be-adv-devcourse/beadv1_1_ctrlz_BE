package com.domainservice.domain.post.favorite.controller;

import static com.common.exception.vo.ProductPostExceptionCode.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.common.model.web.PageResponse;
import com.domainservice.domain.post.favorite.docs.AddFavoriteApiDocs;
import com.domainservice.domain.post.favorite.docs.CancelFavoriteApiDocs;
import com.domainservice.domain.post.favorite.docs.CheckFavoriteStatusApiDocs;
import com.domainservice.domain.post.favorite.docs.GetMyFavoriteListApiDocs;
import com.domainservice.domain.post.favorite.model.dto.FavoritePostResponse;
import com.domainservice.domain.post.favorite.model.dto.FavoriteProductResponse;
import com.domainservice.domain.post.favorite.model.dto.FavoriteStatusResponse;
import com.domainservice.domain.post.favorite.service.FavoriteService;
import com.domainservice.domain.post.post.exception.ProductPostException;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 관심 상품(찜하기) 관련 API를 제공하는 컨트롤러입니다.
 * 상품 좋아요 등록/취소, 내 관심 목록 조회, 좋아요 여부 확인 기능을 제공합니다.
 */
@Tag(name = "Favorite Product", description = "관심 상품(찜하기) API")
@RestController
@RequestMapping("/api/users/favorites")
@RequiredArgsConstructor
public class FavoriteProductController {

	private final FavoriteService favoriteService;

	/**
	 * 관심 상품 등록 API
	 * 특정 상품을 사용자의 관심 상품(좋아요) 목록에 추가합니다.
	 *
	 * @param userId 요청 헤더(X-REQUEST-ID)에서 추출한 사용자 ID (필수)
	 * @param productPostId 상품 게시글 ID
	 * @return 등록된 상품 Id와 성공 메시지
	 * @throws ProductPostException 인증되지 않은 사용자(anonymous)이거나 이미 등록된 상품인 경우
	 */
	@AddFavoriteApiDocs
	@PostMapping("/{productPostId}")
	@ResponseStatus(HttpStatus.CREATED)
	public BaseResponse<FavoritePostResponse> addFavoriteProduct(
		@RequestHeader(value = "X-REQUEST-ID", defaultValue = "anonymous") String userId,
		@PathVariable String productPostId
	) {
		validateAuthentication(userId);
		FavoritePostResponse response = favoriteService.addFavoriteProduct(userId, productPostId);
		return new BaseResponse<>(response, "관심 상품 등록에 성공했습니다.");
	}

	/**
	 * 관심 상품 취소 API
	 * 등록된 관심 상품을 목록에서 제거합니다.
	 *
	 * @param userId 요청 헤더(X-REQUEST-ID)에서 추출한 사용자 ID (필수)
	 * @param productPostId 상품 게시글 ID
	 * @return 취소된 상품 Id와 취소 성공 메시지
	 * @throws ProductPostException 인증되지 않은 사용자이거나 등록 내역이 없는 경우
	 */
	@CancelFavoriteApiDocs
	@DeleteMapping("/{productPostId}")
	@ResponseStatus(HttpStatus.OK)
	public BaseResponse<FavoritePostResponse> cancelFavoriteProduct(
		@RequestHeader(value = "X-REQUEST-ID", defaultValue = "anonymous") String userId,
		@PathVariable String productPostId
	) {
		validateAuthentication(userId);
		FavoritePostResponse response = favoriteService.cancelFavoriteProduct(userId, productPostId);
		return new BaseResponse<>(response, "관심 상품 취소에 성공했습니다.");
	}

	/**
	 * 내 관심 상품 목록 조회 API
	 * 사용자가 등록한 관심 상품 목록을 페이징 처리하여 조회합니다.
	 *
	 * @param userId 요청 헤더(X-REQUEST-ID)에서 추출한 사용자 ID (필수)
	 * @param pageable 페이징 정보 (기본값: size=20, sort=createdAt DESC)
	 * @return 페이징된 관심 상품 목록과 페이지 정보를 포함한 응답 객체
	 * @throws ProductPostException 인증되지 않은 사용자일 경우
	 */
	@GetMyFavoriteListApiDocs
	@GetMapping("/my")
	@ResponseStatus(HttpStatus.OK)
	public PageResponse<List<FavoriteProductResponse>> getMyFavoriteProductList(
		@RequestHeader(value = "X-REQUEST-ID", defaultValue = "anonymous") String userId,
		@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		validateAuthentication(userId);
		return favoriteService.getMyFavoriteProductList(userId, pageable);
	}

	/**
	 * 관심 상품 여부 조회 API
	 * 특정 상품에 대해 사용자가 '좋아요'를 눌렀는지 상태를 확인합니다.
	 *
	 * @param userId 요청 헤더(X-REQUEST-ID)에서 추출한 사용자 ID (필수)
	 * @param productPostId 상품 게시글 ID
	 * @return 해당 상품에 대한 관심 등록 여부(Boolean)를 포함한 응답 객체
	 * @throws ProductPostException 인증되지 않은 사용자일 경우
	 */
	@CheckFavoriteStatusApiDocs
	@GetMapping("/{productPostId}/status")
	@ResponseStatus(HttpStatus.OK)
	public BaseResponse<FavoriteStatusResponse> checkWishListStatus(
		@RequestHeader(value = "X-REQUEST-ID", defaultValue = "anonymous") String userId,
		@PathVariable String productPostId
	) {
		validateAuthentication(userId);
		FavoriteStatusResponse response = favoriteService.isFavorite(userId, productPostId);
		return new BaseResponse<>(response, "해당 상품에 대한 좋아요 여부 조회를 성공했습니다.");
	}

	/*
    ================= private Method =================
     */

	// 사용자 인증 검증 (헤더 값이 'anonymous'인 경우 예외 처리)
	private void validateAuthentication(String userId) {
		if (userId.equals("anonymous")) {
			throw new ProductPostException(UNAUTHORIZED);
		}
	}

}