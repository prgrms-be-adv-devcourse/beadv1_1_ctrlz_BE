package com.domainservice.domain.search.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.domainservice.domain.search.model.entity.persistence.SearchWordLog;

@Repository
public interface SearchWordLogJpaRepository
	extends JpaRepository<SearchWordLog, String> {
}
