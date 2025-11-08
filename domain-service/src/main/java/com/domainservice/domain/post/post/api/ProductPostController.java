package com.domainservice.domain.post.post.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.domainservice.domain.asset.image.application.ImageService;
import com.domainservice.domain.post.post.service.ProductPostService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product-posts")
public class ProductPostController {

    private final ProductPostService productPostService;
    private final ImageService imageService;

    // /**
    // * 상품 게시글 생성 (이미지 포함)
    //  */
    // @ResponseStatus(HttpStatus.CREATED)
    // @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // public BaseResponse<ProductPostResponse> createProductPost(
    //         // @AuthenticationPrincipal String userId,
    //         @RequestPart(value = "images", required = false) List<MultipartFile> images,
    //         @Valid @RequestPart("request") CreateProductPostRequest request
    // ) {
    //     String userId = "user-id";  // TODO: 실제로는 인증된 사용자 ID
	//
    //     List<Image> uploadedImages = null;
    //     if (images != null && !images.isEmpty()) {
    //         uploadedImages = imageService.uploadProductImages(images);
    //     }
	//
    //     ProductPostResponse response = productPostService.createProductPost(
    //             request, userId, uploadedImages
    //     );
	//
    //     return new BaseResponse<>(response, "상품 게시글이 생성되었습니다.");
    // }
	//
    // /**
    //  * 상품 게시글 삭제
    //  */
    // @DeleteMapping("/{postId}")
    // @ResponseStatus(HttpStatus.OK)
    // public BaseResponse<String> deleteProductPost(
    //         // @AuthenticationPrincipal String userId,
    //         @PathVariable String postId
    // ) {
    //     String userId = "user-id";  // TODO: 실제로는 인증된 사용자 ID를 사용
    //     String response = productPostService.deleteProductPost(userId, postId);
    //     return new BaseResponse<>(response, "상품 게시글이 삭제되었습니다.");
    // }
	//
    // /**
    //  * 상품 게시글 수정
    //  */
    // @PatchMapping("/{postId}")
    // @ResponseStatus(HttpStatus.OK)
    // public BaseResponse<ProductPostResponse> updateProductPost(
    //         // @AuthenticationPrincipal String userId,
    //         @PathVariable String postId,
    //         @Valid @RequestBody UpdateProductPostRequest request
    //         // TODO: 실제 파일로 받아오기
    // ) {
    //     String userId = "user-id";  // TODO: 실제로는 인증된 사용자 ID를 사용
    //     ProductPostResponse response = productPostService.updateProductPost(userId, postId, request);
    //     return new BaseResponse<>(response, "상품 게시글이 수정되었습니다.");
    // }
	//
    // /**
    //  * 단일 게시글 조회
    //  */
    // @GetMapping("{postId}")
    // @ResponseStatus(HttpStatus.OK)
    // public BaseResponse<ProductPostResponse> getProductPostById(@PathVariable String postId) {
    //     ProductPostResponse response = productPostService.getProductPostById(postId);
    //     return new BaseResponse<>(response, "상품 게시글이 조회되었습니다.");
    // }
	//
    // /**
    //  * 상품 게시글 목록 조회 (페이징 + 필터링)
    //  */
    // @GetMapping
    // @ResponseStatus(HttpStatus.OK)
    // public PageResponse<List<ProductPostResponse>> getProductPostList(
    //         @PageableDefault(size = 20, sort = "createdAt",
    //                 direction = Sort.Direction.DESC) Pageable pageable,
    //         @RequestParam(required = false) String categoryId,
    //         @RequestParam(required = false) ProductStatus status,
    //         @RequestParam(required = false) TradeStatus tradeStatus,
    //         @RequestParam(required = false) Integer minPrice,
    //         @RequestParam(required = false) Integer maxPrice
    // ) {
    //     PageResponse<List<ProductPostResponse>> response = productPostService.getProductPostList(
    //             pageable, categoryId, status, tradeStatus, minPrice, maxPrice
    //     );
    //     return response;
    // }

}