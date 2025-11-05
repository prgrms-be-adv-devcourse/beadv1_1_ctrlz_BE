package com.domainservice.domain.product.product.api;

import com.common.model.web.BaseResponse;
import com.domainservice.domain.product.product.model.dto.request.CreateProductPostRequest;
import com.domainservice.domain.product.product.model.dto.response.ProductPostResponse;
import com.domainservice.domain.product.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product-posts")
public class ProductController {

    private final ProductService productService;

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
        ProductPostResponse response = productService.createProductPost(request, userId);

        return new BaseResponse<>(response, "상품 게시글이 생성되었습니다.");
    }

}