package com.domainservice.domain.post.post.api;

import static com.common.exception.vo.ProductPostExceptionCode.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.common.model.web.BaseResponse;
import com.common.model.web.PageResponse;
import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.model.dto.request.ProductPostRequest;
import com.domainservice.domain.post.post.model.dto.response.ProductPostResponse;
import com.domainservice.domain.post.post.model.enums.ProductStatus;
import com.domainservice.domain.post.post.model.enums.TradeStatus;
import com.domainservice.domain.post.post.service.ProductPostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 상품 게시글 관련 REST API를 제공하는 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product-posts")
public class ProductPostController {

	private final ProductPostService productPostService;

	/**
	 * 상품 게시글을 생성합니다.
	 *
	 * @param imageFiles 업로드할 이미지 파일 목록 (최소 1개 필수)
	 * @param request    게시글 생성 요청 정보
	 * @return 생성된 게시글 정보 (201 Created)
	 */
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public BaseResponse<ProductPostResponse> createProductPost(
		@RequestHeader(value = "X-REQUEST-ID", required = false, defaultValue = "anonymous") String userId,
		@RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
		@Valid @RequestPart("request") ProductPostRequest request
	) {
		validateAuthentication(userId);
		ProductPostResponse response = productPostService.createProductPost(request, userId, imageFiles);
		return new BaseResponse<>(response, "상품 게시글이 생성되었습니다.");
	}

	/**
	 * 상품 게시글을 수정합니다. 기존 이미지는 삭제되고 새 이미지로 교체됩니다.
	 *
	 * @param postId     수정할 게시글 ID
	 * @param imageFiles 새로운 이미지 파일 목록 (필수)
	 * @param request    게시글 수정 요청 정보
	 * @return 수정된 게시글 정보 (200 OK)
	 */
	@ResponseStatus(HttpStatus.OK)
	@PatchMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public BaseResponse<ProductPostResponse> updateProductPost(
		@RequestHeader(value = "X-REQUEST-ID", defaultValue = "anonymous") String userId,
		@PathVariable String postId,
		@RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
		@Valid @RequestPart("request") ProductPostRequest request
	) {
		validateAuthentication(userId);
		ProductPostResponse response = productPostService.updateProductPost(request, imageFiles, userId, postId);
		return new BaseResponse<>(response, "상품 게시글이 수정되었습니다.");
	}

	/**
	 * 상품 게시글을 삭제합니다. (Soft Delete)
	 *
	 * @param postId 삭제할 게시글 ID
	 * @return 삭제된 게시글 ID (200 OK)
	 */
	@DeleteMapping("/{postId}")
	@ResponseStatus(HttpStatus.OK)
	public BaseResponse<String> deleteProductPost(
		@RequestHeader(value = "X-REQUEST-ID", defaultValue = "anonymous") String userId,
		@PathVariable String postId
	) {
		validateAuthentication(userId);
		String response = productPostService.deleteProductPost(userId, postId);
		return new BaseResponse<>(response, "상품 게시글이 삭제되었습니다.");
	}

	/**
	 * 단일 상품 게시글을 조회합니다. 조회 시 조회수가 증가합니다.
	 *
	 * @param postId 조회할 게시글 ID
	 * @return 게시글 상세 정보 (200 OK)
	 */
	@GetMapping("/{postId}")
	@ResponseStatus(HttpStatus.OK)
	public BaseResponse<ProductPostResponse> getProductPostById(
		@RequestHeader(value = "X-REQUEST-ID", defaultValue = "anonymous") String userId,
		@PathVariable String postId
	) {
		ProductPostResponse response = productPostService.getProductPostById(userId, postId);
		return new BaseResponse<>(response, "상품 게시글이 조회되었습니다.");
	}

	/**
	 * 상품 게시글 목록을 페이징하여 조회합니다. 동적 필터링을 지원합니다.
	 *
	 * @param pageable    페이징 정보 (기본값: size=20, sort=createdAt, DESC)
	 * @param categoryId  카테고리 ID (선택)
	 * @param status      상품 상태 (선택)
	 * @param tradeStatus 거래 상태 (선택)
	 * @param minPrice    최소 가격 (선택)
	 * @param maxPrice    최대 가격 (선택)
	 * @return 페이징된 게시글 목록 (200 OK)
	 */
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public PageResponse<List<ProductPostResponse>> getProductPostList(
		@PageableDefault(size = 20, sort = "createdAt",
			direction = Sort.Direction.DESC) Pageable pageable,
		@RequestParam(required = false) String categoryId,
		@RequestParam(required = false) ProductStatus status,
		@RequestParam(required = false) TradeStatus tradeStatus,
		@RequestParam(required = false) Integer minPrice,
		@RequestParam(required = false) Integer maxPrice
	) {
		PageResponse<List<ProductPostResponse>> response = productPostService.getProductPostList(
			pageable, categoryId, status, tradeStatus, minPrice, maxPrice
		);
		return response;
	}

	/**
	 * 최근 본 상품 목록을 조회합니다.
	 *
	 * @param userId 사용자 식별자 (X-REQUEST-ID 헤더에서 추출)
	 * @return 최근 본 상품 목록 (200 OK)
	 */
	@GetMapping("/recent-views")
	public BaseResponse<List<ProductPostResponse>> getRecentlyViewPosts(
		@RequestHeader(value = "X-REQUEST-ID", defaultValue = "anonymous") String userId
	) {
		validateAuthentication(userId);
		List<ProductPostResponse> recentlyViewedPostList = productPostService.getRecentlyViewedPosts(userId);
		return new BaseResponse<>(recentlyViewedPostList, "최근 본 상품 목록 조회가 완료되었습니다.");
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
