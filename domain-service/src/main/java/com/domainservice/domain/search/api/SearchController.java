package com.domainservice.domain.search.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.domainservice.domain.search.docs.GetAutoCompletionApiDocs;
import com.domainservice.domain.search.docs.word.SaveSearchWordApiDocs;
import com.domainservice.domain.search.model.dto.response.SearchWordResponse;
import com.domainservice.domain.search.service.SearchWordElasticService;
import com.domainservice.domain.search.service.SearchWordRedisService;
import com.domainservice.domain.search.service.dto.request.Prefix;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/searches")
public class SearchController {

	private final SearchWordElasticService searchWordElasticService;
	private final SearchWordRedisService searchWordRedisService;

	/**
	 * 검색어 자동완성 api
	 * @param prefix 검색창에 입력한 단어
 	 * @param userId 사용자 id
	 * @return
	 */
	@GetAutoCompletionApiDocs
	@GetMapping("/suggestion")
	@ResponseStatus(HttpStatus.OK)
	public BaseResponse<List<SearchWordResponse>> getAutoCompletionList(
		@RequestParam(required = false) String prefix,
		@RequestHeader(value = "X-REQUEST-ID", required = false) String userId
	) {
		List<SearchWordResponse> response = searchWordElasticService.getAutoCompletionWordList(
			new Prefix(prefix), userId
		);

		return new BaseResponse<>(
			response,
			"검색어 자동완성 리스트 응답 성공"
		);
	}

	/**
	 * 검색어 저장 -> Redis에 저장
	 * @param searchWord
	 * @param userId
	 * @return
	 */
	@SaveSearchWordApiDocs
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BaseResponse<Void> addSearchWord(
		@RequestBody String searchWord,
		@RequestHeader(value = "X-REQUEST-ID", required = false) String userId
	) {

		searchWordRedisService.saveSearchWord(searchWord, userId);
		return new BaseResponse<>(
			null,
			"검색어 저장 완료"
		);
	}

	@GetMapping("/trend")
	@ResponseStatus(HttpStatus.OK)
	public BaseResponse<List<SearchWordResponse>> getTrendWordList() {
		List<SearchWordResponse> response = searchWordRedisService.getTrendWordList();
		return new BaseResponse<>(
			response,
			""
		);
	}

	/**
	 * 일간 인기 검색어 조회.
	 * @return
	 */
	@GetMapping("/popular-daily")
	@ResponseStatus(HttpStatus.OK)
	public BaseResponse<List<SearchWordResponse>> getDailyPopularWord() {
		List<SearchWordResponse> response = searchWordRedisService.getDailyPopularWord();
		return new BaseResponse<>(
			response,
			""
		);
	}

	/**
	 * 검색어 저장 test api
	 * @param userId
	 * @return
	 */
	@Hidden
	@PostMapping("test")
	@ResponseStatus(HttpStatus.CREATED)
	public BaseResponse<Void> addSearchWord(
		@RequestHeader(value = "X-REQUEST-ID", required = false) String userId
	) {

		searchWordRedisService.saveSearchWordForTest();
		return new BaseResponse<>(
			null,
			"검색어 저장 완료"
		);
	}
}
