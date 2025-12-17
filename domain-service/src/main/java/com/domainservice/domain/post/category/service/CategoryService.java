package com.domainservice.domain.post.category.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.domainservice.domain.post.category.model.dto.response.CategoryResponse;
import com.domainservice.domain.post.category.model.entity.Category;
import com.domainservice.domain.post.category.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
     * 전체 카테고리 ID 목록 조회 (초기화용)
     */
    public List<String> getAllCategoryIds() {
        return categoryRepository.findAll()
                .stream()
                .map(Category::getId)
                .toList();
    }

}
