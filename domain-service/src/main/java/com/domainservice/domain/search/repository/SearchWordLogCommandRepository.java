package com.domainservice.domain.search.repository;

import java.util.List;

import com.domainservice.domain.search.model.entity.persistence.SearchWordLog;

public interface SearchWordLogCommandRepository {

	List<SearchWordLog> insertAll(List<SearchWordLog> logs);
}
