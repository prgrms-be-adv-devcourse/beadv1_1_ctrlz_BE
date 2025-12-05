package com.domainservice.domain.search.repository.jpa;

import java.util.List;

import com.domainservice.domain.search.model.entity.persistence.SearchWordLog;

public interface SearchWordLogCommandRepository {

	void insertAll(List<SearchWordLog> logs);
}
