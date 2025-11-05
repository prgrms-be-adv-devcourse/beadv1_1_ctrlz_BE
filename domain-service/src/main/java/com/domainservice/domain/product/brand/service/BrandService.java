package com.domainservice.domain.product.brand.service;

import com.domainservice.domain.product.brand.model.dto.response.BrandResponse;
import com.domainservice.domain.product.brand.model.entity.Brand;
import com.domainservice.domain.product.brand.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BrandService {

    private final BrandRepository brandRepository;

    /**
     * 전체 브랜드 조회
     */
    public List<BrandResponse> getBrands() {
        return brandRepository.findAll()
                .stream()
                .map(BrandResponse::from)
                .toList();
    }

    /**
     * 브랜드 생성
     */
    @Transactional
    public Brand createBrand(String name) {
        log.info("브랜드 생성: {}", name);

        Brand brand = Brand.builder()
                .name(name)
                .build();

        return brandRepository.save(brand);
    }

    /**
     * 브랜드 존재 여부 확인
     */
    public boolean existsByName(String name) {
        return brandRepository.existsByName(name);
    }

    /**
     * 브랜드가 없으면 생성 (초기화용)
     */
    @Transactional
    public Brand createIfNotExists(String name) {
        if (!existsByName(name)) {
            return createBrand(name);
        }
        log.info("브랜드 이미 존재: {}", name);
        return brandRepository.findByName(name).orElseThrow();
    }
}