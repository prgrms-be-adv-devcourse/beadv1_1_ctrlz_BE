package com.domainservice.domain.post.tag.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.domainservice.domain.post.tag.docs.GetTagsApiDocs;
import com.domainservice.domain.post.tag.model.dto.response.TagResponse;
import com.domainservice.domain.post.tag.service.TagService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 태그 관련 API를 제공하는 컨트롤러입니다.
 * 게시글 작성 시 사용 가능한 태그 목록을 조회하는 기능을 제공합니다.
 */
@Tag(name = "Tag", description = "태그 API")
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

	private final TagService tagService;

	/**
	 * 태그 목록 조회 API
	 * 시스템에서 사용 가능한 모든 태그 목록을 조회합니다.
	 *
	 * @return 태그 목록과 성공 메시지
	 */
	@GetTagsApiDocs
	@GetMapping
	public BaseResponse<List<TagResponse>> getTags() {
		List<TagResponse> tags = tagService.getTags();
		return new BaseResponse<>(tags, "태그 목록 조회에 성공했습니다.");
	}
}