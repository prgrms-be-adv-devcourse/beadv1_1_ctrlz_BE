package com.domainservice.domain.search.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.domainservice.domain.search.model.entity.persistence.SearchWordLog;
import com.domainservice.domain.search.service.dto.request.Prefix;
import com.domainservice.domain.search.model.dto.response.SearchWordResponse;
import com.domainservice.domain.search.model.entity.dto.document.SearchWordDocumentEntity;
import com.domainservice.domain.search.repository.SearchWordRepository;
import com.domainservice.domain.search.service.analyzer.PrefixAnalyzer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchWordElasticService {

	private final PrefixAnalyzer prefixAnalyzer;

	private final SearchWordRedisService searchWordRedisService;

	private final SearchWordRepository searchWordRepository;

	/**
	 * 검색어를 받음
	 * 	검색어가 없으면? -> 최근에 내가 검색한 목록들 조회.
	 * 	검색어가 한글이면? -> qwertyInput으로 변환 후 qwertyInput으로 조회 + 한글로 조회
	 * 	검색어가 영어면?
	 * 	 1. 사전에 있는 단어면? -> 한글로 번역 작업?
	 *   2. 사전에 없는 단어면? -> qwertyInput이므로, qwertyInput으로 조회 (+ 유사도가 높은 한글 단어로 재조회)
	 * ---
	 * TODO:
	 *  1. 영어로 입력하면 영어자체로도 같이 반환할수 있도록 전환 예정(데이터셋이 많이 필요할듯)
	 *    DocumentEntity의 koreanWord -> originalWord로 전환해서 리팩토링 예정
	 *  2. 자음만 입력해도 결과 반환할수 있도록 수정
	 * @param prefix
	 * @param userId
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<SearchWordResponse> getAutoCompletionWordList(Prefix prefix, String userId) {
		List<SearchWordResponse> responseList = new ArrayList<>();

		log.info("originValue = {}", prefix.value());
		log.info("qwertyValue = {}", prefix.getQwertyInput());

		//검색어가 없는 경우
		String originValue = prefix.value();
		if (!StringUtils.hasText(originValue)) {
			// Redis에서 내가 검색한 키워드 리스트 조회.
			return !userId.isEmpty() ?
				getRecentSearchKeywords(userId) :
				searchWordRedisService.getTrendWordList();
		}

		String convertedValue = prefix.getQwertyInput();

		List<SearchWordDocumentEntity> findWordList;
		if (!prefixAnalyzer.existsInDictionary(convertedValue)) {
			//사전에 없는 영어입력값이면 -> convertedValue와 일치하는 검색후 -> 유사 검색
			findWordList = findWordListForUnknownEnglish(prefix);
		} else {
			//사전에 있거나 한글이면.
			findWordList = findWordListForOrigin(prefix);
		}

		log.info("findWordList = {}", findWordList);
		//koreanWord값이 같으면 중복처리
		findWordList.stream()
			.distinct()
			.map(SearchWordResponse::from)
			.forEach(responseList::add);

		return responseList;
	}

	private List<SearchWordResponse> getRecentSearchKeywords(String userId) {
		return searchWordRedisService.getRecentSearchWordList(userId);
	}

	/**
	 * 사전에 없는 영어일 경우에 조회 -> 실제 의미는 한글
	 * @param prefix
	 * @return
	 */
	private List<SearchWordDocumentEntity> findWordListForUnknownEnglish(Prefix prefix) {
		SearchWordDocumentEntity findEntity = searchWordRepository.findByQwertyInput(prefix.value())
			.orElseGet(() -> SearchWordDocumentEntity.createDocumentEntity(
				SearchWordLog.create(prefix.value(), LocalDateTime.now())
			));

		return findWordListForOrigin(new Prefix(findEntity.getOriginValue()));
	}

	/**
	 * 한글일 때 조회
	 * @param prefix 사용자가 한국어로 입력한 값
	 * @return
	 */
	private List<SearchWordDocumentEntity> findWordListForOrigin(Prefix prefix) {
		String originValue = prefix.value();

		List<SearchWordDocumentEntity> findSearchWord = new ArrayList<>(
			searchWordRepository.findAllByOriginValue(originValue)
		);

		log.info("findSearchWord = {}", findSearchWord);
		return findSearchWord;
	}
}
