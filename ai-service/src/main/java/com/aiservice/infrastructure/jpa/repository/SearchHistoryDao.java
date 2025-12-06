package com.aiservice.infrastructure.jpa.repository;

import org.springframework.stereotype.Repository;

import com.aiservice.domain.model.SearchHistory;
import com.aiservice.domain.repository.SearchHistoryRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class SearchHistoryDao implements SearchHistoryRepository {

	private final SearchHistoryJpaRepository searchHistoryJpaRepository;

	@Override
	public void save(SearchHistory searchHistory) {
		searchHistoryJpaRepository.save(searchHistory);
	}

	@Override
	public SearchHistory findById(Long id) {
		return searchHistoryJpaRepository.findById(id).orElse(null);
	}
}
