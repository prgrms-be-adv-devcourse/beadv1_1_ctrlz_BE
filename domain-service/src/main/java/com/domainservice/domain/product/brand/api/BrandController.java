package com.domainservice.domain.product.brand.api;

import com.common.model.web.BaseResponse;
import com.domainservice.domain.product.brand.model.dto.response.BrandResponse;
import com.domainservice.domain.product.brand.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public BaseResponse<List<BrandResponse>> getBrands() {

        List<BrandResponse> brands = brandService.getBrands();

        return new BaseResponse<>(
                brands,
                "브랜드 목록을 조회합니다."
        );

    }
}