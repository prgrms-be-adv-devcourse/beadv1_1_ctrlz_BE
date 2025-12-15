package com.domainservice.common.init.data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.domainservice.common.init.dummy.DummyDataLoader;
import com.github.f4b6a3.uuid.UuidCreator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class CategoryInitializer {

	private final JdbcTemplate jdbcTemplate;
	private final DummyDataLoader dataLoader;

	@Transactional
	public void init() {
		log.info("--- 카테고리 초기화 시작 ---");

		List<String> categoryNames = dataLoader.loadLines("init/categories.txt");

		// 기존 카테고리 조회 (중복 체크)
		List<String> existingCategories = jdbcTemplate.queryForList(
			"SELECT name FROM category WHERE delete_status = 'N'",
			String.class
		);

		List<Object[]> batchData = new ArrayList<>();
		LocalDateTime now = LocalDateTime.now();
		int skippedCount = 0;

		for (String categoryName : categoryNames) {
			if (categoryName.trim().isEmpty() || categoryName.trim().startsWith("#")) {
				continue;
			}

			if (existingCategories.contains(categoryName)) {
				skippedCount++;
				continue;
			}

			// UUIDv7로 ID 자동 생성
			String categoryId = UuidCreator.getTimeOrderedEpoch().toString();
			batchData.add(new Object[] {categoryId, categoryName, "N", now, now});
		}

		if (!batchData.isEmpty()) {
			jdbcTemplate.batchUpdate(
				"""
					INSERT INTO category (id, name, delete_status, created_at, updated_at)
					VALUES (?, ?, ?, ?, ?)
					""",
				batchData
			);
			log.info("--- 카테고리 초기화 완료: {}개 생성, {}개 건너뜀 ---",
				batchData.size(), skippedCount);
		} else {
			log.info("--- 카테고리 초기화 완료: 모든 카테고리가 이미 존재함 ({}/{}개) ---",
				skippedCount, categoryNames.size());
		}
	}
}