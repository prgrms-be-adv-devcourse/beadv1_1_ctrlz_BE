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
import com.domainservice.domain.post.favorite.model.dto.FavoriteProductResponse;
import com.domainservice.domain.post.favorite.model.dto.FavoriteStatusResponse;
import com.domainservice.domain.post.favorite.model.dto.FavoriteToggleResponse;
import com.domainservice.domain.post.favorite.service.FavoriteService;
import com.domainservice.domain.post.post.exception.ProductPostException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users/favorites")
@RequiredArgsConstructor
public class FavoriteProductController {

	private final FavoriteService favoriteService;

	@PostMapping("/{productPostId}")
	@ResponseStatus(HttpStatus.CREATED)
	public BaseResponse<FavoriteToggleResponse> addFavoriteProduct(
		@RequestHeader(value = "X-REQUEST-ID", defaultValue = "anonymous") String userId,
		@PathVariable String productPostId
	) {
		validateAuthentication(userId);
		FavoriteToggleResponse response = favoriteService.addFavoriteProduct(userId, productPostId);
		return new BaseResponse<>(response, "찜하기 성공");
	}

	@DeleteMapping("/{productPostId}")
	@ResponseStatus(HttpStatus.OK)
	public BaseResponse<FavoriteToggleResponse> cancelFavoriteProduct(
		@RequestHeader(value = "X-REQUEST-ID", defaultValue = "anonymous") String userId,
		@PathVariable String productPostId
	) {
		validateAuthentication(userId);
		FavoriteToggleResponse response = favoriteService.cancelFavoriteProduct(userId, productPostId);
		return new BaseResponse<>(response, "찜하기 취소 성공");
	}

	@GetMapping("/my")
	@ResponseStatus(HttpStatus.OK)
	public PageResponse<List<FavoriteProductResponse>> getMyFavoriteProductList(
		@RequestHeader(value = "X-REQUEST-ID", defaultValue = "anonymous") String userId,
		@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		validateAuthentication(userId);
		return favoriteService.getMyFavoriteProductList(userId, pageable);
	}

	@GetMapping("/{productPostId}/status")
	@ResponseStatus(HttpStatus.OK)
	public BaseResponse<FavoriteStatusResponse> checkWishListStatus(
		@RequestHeader(value = "X-REQUEST-ID", defaultValue = "anonymous") String userId,
		@PathVariable String productPostId
	) {
		validateAuthentication(userId);
		FavoriteStatusResponse response = favoriteService.isFavorite(userId, productPostId);
		return new BaseResponse<>(response, "찜하기 여부 조회 성공");
	}

	/*
    ================= private Method =================
     */

	private void validateAuthentication(String userId) {
		if (userId.equals("anonymous")) {
			throw new ProductPostException(UNAUTHORIZED);
		}
	}

}