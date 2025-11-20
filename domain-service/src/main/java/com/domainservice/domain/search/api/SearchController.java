package com.domainservice.domain.search.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.domainservice.domain.search.model.dto.request.Prefix;
import com.domainservice.domain.search.model.dto.response.SearchWordResponse;
import com.domainservice.domain.search.service.SearchWordElasticService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/searches")
public class SearchController {

	private final SearchWordElasticService searchWordElasticService;


	@GetMapping("/suggestion/{userId}")
	public BaseResponse<List<SearchWordResponse>> getAutoCompletionList(
		@RequestParam(required = false) String prefix,
		@PathVariable(required = false) String userId
	) {
		List<SearchWordResponse> response = searchWordElasticService.getAutoCompletionWordList(
			new Prefix(prefix), userId
		);

		return new BaseResponse<>(
			response,
			"검색어 자동완성 리스트 응답 성공"
		);
	}
}
