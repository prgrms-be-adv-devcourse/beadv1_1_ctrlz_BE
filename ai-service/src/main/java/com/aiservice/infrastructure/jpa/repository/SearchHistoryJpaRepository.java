package com.aiservice.infrastructure.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aiservice.domain.model.SearchHistory;


public interface SearchHistoryJpaRepository extends JpaRepository<SearchHistory, Long> {
}
