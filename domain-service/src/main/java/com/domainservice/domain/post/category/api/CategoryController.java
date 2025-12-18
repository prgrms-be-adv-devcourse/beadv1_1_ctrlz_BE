package com.domainservice.domain.post.category.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.domainservice.domain.post.category.api.docs.GetCategoriesApiDocs;
import com.domainservice.domain.post.category.model.dto.response.CategoryResponse;
import com.domainservice.domain.post.category.service.CategoryService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 카테고리 관련 API를 제공하는 컨트롤러입니다.
 * 게시글 작성 시 선택 가능한 카테고리 목록을 조회하는 기능을 제공합니다.
 */
@Tag(name = "Category", description = "카테고리 API")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	/**
	 * 카테고리 목록 조회 API
	 * 시스템에서 사용 가능한 모든 카테고리 목록을 조회합니다.
	 *
	 * @return 카테고리 목록과 성공 메시지
	 */
	@GetCategoriesApiDocs
	@GetMapping
	public BaseResponse<List<CategoryResponse>> getCategories() {
		List<CategoryResponse> categories = categoryService.getCategories();
		return new BaseResponse<>(categories, "카테고리 목록 조회에 성공했습니다.");
	}
}
