package com.aiservice.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aiservice.domain.model.SearchHistory;
import com.aiservice.domain.repository.SearchHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchHistoryService {

	private final SearchHistoryRepository searchHistoryRepository;

	@Transactional
	public void saveSearchTerm(String userId, String searchTerm) {

		SearchHistory history = SearchHistory.builder()
			.userId(userId)
			.searchTerm(searchTerm)
			.build();

		searchHistoryRepository.save(history);
		log.info("사용자 [{}]의 검색어 [{}]를 저장했습니다.", userId, searchTerm);
	}
}
