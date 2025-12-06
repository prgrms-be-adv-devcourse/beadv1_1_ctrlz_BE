package com.aiservice.domain.repository;

import com.aiservice.domain.model.SearchHistory;

public interface SearchHistoryRepository {

	void save(SearchHistory searchHistory);
	SearchHistory findById(Long id);
}
