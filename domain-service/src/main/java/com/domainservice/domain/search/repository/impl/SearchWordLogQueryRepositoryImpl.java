package com.domainservice.domain.search.repository.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.domainservice.domain.search.repository.jpa.SearchWordLogQueryRepository;
import com.domainservice.domain.search.service.dto.vo.DailyPopularWordLog;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import static com.domainservice.domain.search.model.entity.persistence.QSearchWordLog.searchWordLog;

import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SearchWordLogQueryRepositoryImpl implements SearchWordLogQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Map<String, DailyPopularWordLog> findDailyLogs(LocalDateTime end) {

		LocalDateTime start = end.minusHours(2);

		List<DailyPopularWordLog> rows =
			queryFactory
				.select(Projections.constructor(
					DailyPopularWordLog.class,
					searchWordLog.word,
					searchWordLog.searchedAt.count(),
					searchWordLog.searchedAt.max()
				))
				.from(searchWordLog)
				.where(
					searchWordLog.searchedAt.between(start, end)
				)
				.groupBy(searchWordLog.word)
				.fetch();

		return rows.stream()
			.collect(Collectors.toMap(
				DailyPopularWordLog::word,
				log -> log
			));
	}

}
