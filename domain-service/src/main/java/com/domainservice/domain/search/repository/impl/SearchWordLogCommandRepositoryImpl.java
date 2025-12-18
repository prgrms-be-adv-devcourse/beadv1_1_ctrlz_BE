package com.domainservice.domain.search.repository.impl;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.common.model.persistence.BaseEntity;
import com.domainservice.domain.search.model.entity.persistence.SearchWordLog;
import com.domainservice.domain.search.repository.SearchWordLogCommandRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SearchWordLogCommandRepositoryImpl implements SearchWordLogCommandRepository {

	private final JdbcTemplate jdbcTemplate;
	@Override
	public List<SearchWordLog> insertAll(List<SearchWordLog> logs) {
		String sql = """
			INSERT INTO search_word_logs(id, searched_at, word, created_at, updated_at, delete_status)
			VALUES (?, ?, ?, ?, ?, 'N')
			""".trim();
		int[][] result = jdbcTemplate.batchUpdate(sql, logs, logs.size(),
			(ps, log) -> {
				String generatedId = BaseEntity.createEntityId();

				ps.setString(1, generatedId);
				Timestamp searchedAt = Timestamp.valueOf(log.getSearchedAt());
				ps.setTimestamp(2, searchedAt);
				ps.setString(3, log.getWord());
				ps.setTimestamp(4, searchedAt);
				ps.setTimestamp(5, searchedAt);
			}
		);

		log.info("Batch update results = {}", Arrays.deepToString(result));
		return logs;
	}
}
