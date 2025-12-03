package com.domainservice.common.init.data;

import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.domainservice.common.init.dummy.DummyDataLoader;

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

		Map<String, String> categories = dataLoader.loadCategories("init/categories.csv");
		int createdCount = 0;

		for (Map.Entry<String, String> entry : categories.entrySet()) {
			String id = entry.getKey();
			String name = entry.getValue();

			try {
				// 이미 존재하는지 확인
				Integer count = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM category WHERE id = ?",
					Integer.class,
					id
				);

				if (count != null && count == 0) {
					jdbcTemplate.update(
						"""
							INSERT INTO category (id, name, delete_status, created_at, updated_at)
							VALUES (?, ?, 'N', NOW(), NOW())
							""",
						id, name
					);
					createdCount++;
				}
			} catch (Exception e) {
				log.error("❌ 카테고리 생성 실패: {} ({})", name, id, e);
			}
		}

		log.info("--- 카테고리 초기화 완료 ---");
		log.info("생성: {}개", createdCount);
	}
}