package com.domainservice.domain.post.post.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.domainservice.domain.post.post.mapper.ProductPostMapper;
import com.domainservice.domain.post.post.model.dto.request.CreateProductPostRequest;
import com.domainservice.domain.post.post.model.dto.response.ProductPostResponse;
import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.post.post.service.ProductPostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product-posts")
public class ProductPostController {

	private final ProductPostService productPostService;

	/**
	 * 상품 게시글 생성
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BaseResponse<ProductPostResponse> createProductPost(
		// @AuthenticationPrincipal String userId,
		@Valid @RequestBody CreateProductPostRequest request
	) {
		String userId = "user-id";  // TODO: 실제로는 인증된 사용자 ID를 사용
		ProductPost saved = productPostService.createProductPost(request, userId);

		return new BaseResponse<>(
			ProductPostMapper.toProductPostResponse(saved),
			"상품 게시글이 생성되었습니다."
		);
	}

	/**
	 * 상품 게시글 삭제
	 */
	@DeleteMapping("/{postId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public BaseResponse<String> deleteProductPost(
		// @AuthenticationPrincipal String userId,
		@PathVariable String postId
	) {
		String userId = "user-id";  // TODO: 실제로는 인증된 사용자 ID를 사용
		String deleted = productPostService.deleteProductPost(userId, postId);

		return new BaseResponse<>(
			deleted,
			"상품 게시글이 삭제되었습니다."
		);
	}

}