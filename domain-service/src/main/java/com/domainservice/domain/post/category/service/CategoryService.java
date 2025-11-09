package com.domainservice.domain.post.category.service;

import com.domainservice.domain.post.category.model.dto.response.CategoryResponse;
import com.domainservice.domain.post.category.model.entity.Category;
import com.domainservice.domain.post.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    /**
     * 전체 카테고리 조회
     */
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    /**
     * 카테고리 생성
     */
    @Transactional
    public Category createCategory(String name) {
        log.info("카테고리 생성: {}", name);

        Category category = Category.builder()
                .name(name)
                .build();

        return categoryRepository.save(category);
    }

    /**
     * 카테고리 존재 여부 확인
     */
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    /**
     * 해당 이름의 카테고리가 없으면 생성 (초기화용)
     */
    @Transactional
    public Category createIfNotExists(String name) {
        if (!existsByName(name)) {
            return createCategory(name);
        }
        log.info("카테고리 이미 존재: {}", name);
        return categoryRepository.findByName(name).orElseThrow();
    }

    /**
     * 전체 카테고리 ID 목록 조회 (초기화용)
     */
    public List<String> getAllCategoryIds() {
        return categoryRepository.findAll()
                .stream()
                .map(Category::getId)
                .toList();
    }

}
