package com.domainservice.domain.search.repository.impl;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.domainservice.domain.search.model.entity.persistence.SearchWordLog;
import com.domainservice.domain.search.repository.jpa.SearchWordLogCommandRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SearchWordLogCommandRepositoryImpl implements SearchWordLogCommandRepository {

	private final JdbcTemplate jdbcTemplate;
	@Override
	public void insertAll(List<SearchWordLog> logs) {
		String sql = """
			INSERT INTO search_word_logs(id, searched_at, word, created_at, updated_at, delete_status)
			VALUES (?, ?, ?, now(), now(), 'N')
			""".trim();
		int[][] result = jdbcTemplate.batchUpdate(sql, logs, logs.size(),
			(ps, log) -> {
				ps.setString(1, UUID.randomUUID().toString());
				ps.setTimestamp(2, Timestamp.valueOf(log.getSearchedAt()));
				ps.setString(3, log.getWord());
			}
		);

		log.info("Batch update results = {}", Arrays.deepToString(result));

	}
}
