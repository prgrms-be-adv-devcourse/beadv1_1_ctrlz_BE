package com.domainservice.domain.post.post.api;

import com.common.model.web.BaseResponse;
import com.domainservice.domain.post.post.model.dto.request.CreateProductPostRequest;
import com.domainservice.domain.post.post.model.dto.request.UpdateProductPostRequest;
import com.domainservice.domain.post.post.model.dto.response.ProductPostResponse;
import com.domainservice.domain.post.post.service.ProductPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
            // TODO: 실제 파일로 받아오기
    ) {
        String userId = "user-id";  // TODO: 실제로는 인증된 사용자 ID를 사용
        ProductPostResponse response = productPostService.createProductPost(request, userId);
        return new BaseResponse<>(response, "상품 게시글이 생성되었습니다.");
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
        String response = productPostService.deleteProductPost(userId, postId);
        return new BaseResponse<>(response, "상품 게시글이 삭제되었습니다.");
    }

    /**
     * 상품 게시글 수정
     */
    @PatchMapping("/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<ProductPostResponse> updateProductPost(
            // @AuthenticationPrincipal String userId,
            @PathVariable String postId,
            @Valid @RequestBody UpdateProductPostRequest request
            // TODO: 실제 파일로 받아오기
    ) {
        String userId = "user-id";  // TODO: 실제로는 인증된 사용자 ID를 사용
        ProductPostResponse response = productPostService.updateProductPost(userId, postId, request);
        return new BaseResponse<>(response, "상품 게시글이 수정되었습니다.");
    }

    /**
     * 단일 게시글 조회
     */
    @GetMapping("{postId}")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<ProductPostResponse> getProductPostById(@PathVariable String postId) {
        ProductPostResponse response = productPostService.getProductPostById(postId);
        return new BaseResponse<>(response, "상품 게시글이 조회되었습니다.");
    }

}