// package com.domainservice.domain.search.service;
//
// import static co.elastic.clients.elasticsearch.ingest.Processor.Kind.*;
//
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.util.StringUtils;
//
// import com.domainservice.domain.search.model.dto.response.SearchWordResponse;
// import com.domainservice.domain.search.repository.SearchWordRepository;
//
// import lombok.RequiredArgsConstructor;
//
// @Service
// @RequiredArgsConstructor
// public class SearchWordElasticService {
//
// 	private final SearchWordRepository searchWordRepository;
// 	private final DictionaryService dictionaryService;
//
// 	@Transactional
// 	public SearchWordResponse getAutoCompletionWordList(String prefix) {
// 		/*
// 		 * Todo: 검색어를 받아.
// 		 *  검색어가 없으면? -> 최근에 내가 검색한 목록들 조회.
// 		 *  검색어가 한글이면? -> qwertyInput으로 변환 후 qwertyInput으로 조회 + 한글로 조회
// 		 *  검색어가 영어면?
// 		 *    1. 사전에 있는 단어면? ->
// 		 *    2. 사전에 없는 단어면? -> qwertyInput이므로, qwertyInput으로 조회 (+ 유사도가 높은 한글 단어로 재조회
// 		 */
//
// 		//검색어가 없으면? -> 최근에 내가 검색한 목록들 조회.
// 		if (!StringUtils.hasText(prefix)) {
// 			return null;
// 		}
//
// 		//ElasticSearch에서 조회하기 위한 prefix를 얻는 메서드
// 		dictionaryService.prepareElasticPrefix(prefix);
//
//
//
// 	}
// }
