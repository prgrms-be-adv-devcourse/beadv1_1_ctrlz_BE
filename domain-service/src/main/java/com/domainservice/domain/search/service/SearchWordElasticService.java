package com.domainservice.domain.search.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.domainservice.domain.search.model.entity.persistence.SearchWordLog;
import com.domainservice.domain.search.service.converter.PrefixConverter;
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

		String originValue = prefix.value() != null ? prefix.value().trim() : "";
		String qwertyValue = prefix.getQwertyInput() != null ? prefix.getQwertyInput().trim() : "";

		// qwerty → korean 변환 (dkssud → 안녕)
		String convertedKorean = StringUtils.hasText(qwertyValue)
			? PrefixConverter.convertToKoreanWord(qwertyValue)
			: "";

		log.info("originValue = {}", originValue);
		log.info("qwertyValue = {}", qwertyValue);

		// 1. 검색어가 없는 경우 → 최근 검색어 or 트렌드
		if (!StringUtils.hasText(originValue)) {
			return StringUtils.hasText(userId)
				? getRecentSearchKeywords(userId)
				: searchWordRedisService.getTrendWordList();
		}

		List<SearchWordDocumentEntity> candidates = new ArrayList<>();

		// 2. originValue / 변환된 한글 / qwerty 입력을 모두 기준으로 검색
		if (StringUtils.hasText(originValue)) {
			candidates.addAll(searchWordRepository.findAllByOriginValue(originValue));
		}

		if (StringUtils.hasText(convertedKorean) && !convertedKorean.equals(originValue)) {
			candidates.addAll(searchWordRepository.findAllByOriginValue(convertedKorean));
		}

		// 3. qwerty 변환값이 존재하면 qwerty 기반 검색도 병행
		if (StringUtils.hasText(qwertyValue)) {
			candidates.addAll(searchWordRepository.findAllByQwertyInput(qwertyValue));
		}

		// 4. 그래도 비어있으면 → qwerty 단건 매칭 후 originValue 재시도 (영문 오타 케이스)
		if (candidates.isEmpty() && StringUtils.hasText(qwertyValue)) {
			searchWordRepository.findByQwertyInput(qwertyValue)
				.map(SearchWordDocumentEntity::getOriginValue)
				.ifPresent(origin ->
					candidates.addAll(searchWordRepository.findAllByOriginValue(origin))
				);
		}

		// 5. 중복 제거 + 응답 변환
		candidates.stream()
			.distinct()
			.limit(20)
			.map(SearchWordResponse::from)
			.forEach(responseList::add);

		log.info("auto-complete result size = {}", responseList.size());
		return responseList;
	}

	private List<SearchWordResponse> getRecentSearchKeywords(String userId) {
		return searchWordRedisService.getRecentSearchWordList(userId);
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
