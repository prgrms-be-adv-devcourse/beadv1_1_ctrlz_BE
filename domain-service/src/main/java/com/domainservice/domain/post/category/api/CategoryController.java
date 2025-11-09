package com.domainservice.domain.post.category.api;

import com.common.model.web.BaseResponse;
import com.domainservice.domain.post.category.model.dto.response.CategoryResponse;
import com.domainservice.domain.post.category.service.CategoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	@GetMapping
	public BaseResponse<List<CategoryResponse>> getCategories() {

		List<CategoryResponse> categories = categoryService.getCategories();

		return new BaseResponse<>(
			categories,
			"카테고리 목록을 조회합니다."
		);

	}
}
