// package com.domainservice.domain.search.api;
//
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;
//
// import com.common.model.web.BaseResponse;
// import com.domainservice.domain.search.model.dto.response.SearchWordResponse;
// import com.domainservice.domain.search.service.SearchWordElasticService;
//
// import lombok.RequiredArgsConstructor;
//
// @RestController
// @RequiredArgsConstructor
// @RequestMapping("/api/searches")
// public class SearchController {
//
// 	private final SearchWordElasticService searchWordElasticService;
//
//
// 	@GetMapping
// 	public BaseResponse<SearchWordResponse> getAutoCompletionList(
// 		@RequestParam(required = false) String prefix
// 	) {
// 		SearchWordResponse response = searchWordElasticService.getAutoCompletionWordList(
// 			new ()
// 		);
//
// 		return new BaseResponse<>(
// 			response,
// 			"검색어 자동완성 리스트 응답 성공"
// 		);
// 	}
// }
