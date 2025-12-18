package com.domainservice.common.init.dummy.strategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.domainservice.common.init.dummy.dto.vo.ProductNameData;
import com.domainservice.common.init.dummy.service.DummyDataGenerator;
import com.domainservice.common.init.dummy.service.DummyDataGenerator.TemplateData;
import com.github.f4b6a3.uuid.UuidCreator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 임시 테이블 방식 - DB 임시 테이블 활용
 * 권장: 10만~50만건
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TempTableGeneration implements DummyGeneration {

	private final DummyDataGenerator dataGenerator;

	@Override
	public void generateProducts(int productCount) {
		TemplateData templateData = dataGenerator.loadTemplateData();
		Map<String, String> categoryMapping = dataGenerator.loadCategoryMapping();

		createValueTables(templateData, categoryMapping);
		generateProductPosts(productCount, templateData);
	}

	@Override
	public String getType() {
		return "임시테이블";
	}

	/**
	 * 임시 테이블 생성
	 */
	private void createValueTables(TemplateData templateData, Map<String, String> categoryMapping) {
		JdbcTemplate jdbcTemplate = dataGenerator.getJdbcTemplate();

		// 제목 접두사 테이블
		jdbcTemplate.execute("""
			CREATE TEMPORARY TABLE IF NOT EXISTS title_prefixes (
				id INT AUTO_INCREMENT PRIMARY KEY,
				prefix VARCHAR(50)
			)
			""");
		jdbcTemplate.batchUpdate(
			"INSERT INTO title_prefixes (prefix) VALUES (?)",
			templateData.titlePrefixes(),
			templateData.titlePrefixes().size(),
			(ps, prefix) -> ps.setString(1, prefix)
		);

		// 상품명 테이블
		jdbcTemplate.execute("""
			CREATE TEMPORARY TABLE IF NOT EXISTS product_names (
				id INT AUTO_INCREMENT PRIMARY KEY,
				category_id VARCHAR(255),
				name VARCHAR(200),
				INDEX idx_category_id (category_id)
			)
			""");

		List<Object[]> productNameBatch = new ArrayList<>();
		for (Map.Entry<String, List<String>> entry : templateData.productNames().entrySet()) {
			String categoryId = categoryMapping.get(entry.getKey());
			if (categoryId != null) {
				for (String name : entry.getValue()) {
					productNameBatch.add(new Object[] {categoryId, name});
				}
			}
		}
		if (!productNameBatch.isEmpty()) {
			jdbcTemplate.batchUpdate(
				"INSERT INTO product_names (category_id, name) VALUES (?, ?)",
				productNameBatch
			);
		}

		// 상태 키워드 테이블
		jdbcTemplate.execute("""
			CREATE TEMPORARY TABLE IF NOT EXISTS condition_words (
				id INT AUTO_INCREMENT PRIMARY KEY,
				word VARCHAR(50)
			)
			""");
		jdbcTemplate.batchUpdate(
			"INSERT INTO condition_words (word) VALUES (?)",
			templateData.conditionWords(),
			templateData.conditionWords().size(),
			(ps, word) -> ps.setString(1, word)
		);

		// 설명 접두사 테이블
		jdbcTemplate.execute("""
			CREATE TEMPORARY TABLE IF NOT EXISTS description_prefixes (
				id INT AUTO_INCREMENT PRIMARY KEY,
				prefix TEXT
			)
			""");
		jdbcTemplate.batchUpdate(
			"INSERT INTO description_prefixes (prefix) VALUES (?)",
			templateData.descriptionPrefixes(),
			templateData.descriptionPrefixes().size(),
			(ps, prefix) -> ps.setString(1, prefix)
		);

		// 설명 템플릿 테이블
		jdbcTemplate.execute("""
			CREATE TEMPORARY TABLE IF NOT EXISTS desc_templates (
				id INT AUTO_INCREMENT PRIMARY KEY,
				template TEXT
			)
			""");
		jdbcTemplate.batchUpdate(
			"INSERT INTO desc_templates (template) VALUES (?)",
			templateData.descriptions(),
			templateData.descriptions().size(),
			(ps, desc) -> ps.setString(1, desc)
		);
	}

	/**
	 * 상품 생성
	 */
	private void generateProductPosts(int count, TemplateData templateData) {
		long startTime = System.currentTimeMillis();

		// 임시 테이블에서 상품명 조회
		List<ProductNameData> productNamesList = dataGenerator.getJdbcTemplate().query(
			"SELECT category_id, name FROM product_names",
			(rs, rowNum) -> new ProductNameData(
				rs.getString("category_id"),
				rs.getString("name")
			)
		);

		if (productNamesList.isEmpty()) {
			log.error("임시 테이블이 비어있습니다");
			return;
		}

		List<Object[]> batchData = new ArrayList<>(DummyDataGenerator.BATCH_SIZE);
		Random random = dataGenerator.getRandom();

		int productsPerUser = 5;
		int totalUsers = (int)Math.ceil((double)count / productsPerUser);
		int createdCount = 0;

		int productNamesSize = productNamesList.size();
		int titlePrefixesSize = templateData.titlePrefixes().size();
		int conditionWordsSize = templateData.conditionWords().size();
		int descriptionPrefixesSize = templateData.descriptionPrefixes().size();
		int descriptionsSize = templateData.descriptions().size();
		String[] statuses = {"NEW", "GOOD", "FAIR"};
		String[] tradeStatuses = {"SELLING", "PROCESSING", "SOLDOUT"};

		for (int u = 0; u < totalUsers; u++) {
			String userId = UuidCreator.getTimeOrderedEpoch().toString();

			for (int p = 0; p < productsPerUser && createdCount < count; p++) {
				ProductNameData product = productNamesList.get(random.nextInt(productNamesSize));
				String productId = UuidCreator.getTimeOrderedEpoch().toString();

				String title = product.name() + " " +
					templateData.conditionWords().get(random.nextInt(conditionWordsSize)) + " " +
					templateData.titlePrefixes().get(random.nextInt(titlePrefixesSize));

				String description = product.name() + " " +
					templateData.descriptionPrefixes().get(random.nextInt(descriptionPrefixesSize)) + " " +
					templateData.descriptions().get(random.nextInt(descriptionsSize));

				batchData.add(new Object[] {
					productId, userId, product.categoryId(), title, product.name(),
					(random.nextInt(2001) * 1000) + 500000, description,
					statuses[random.nextInt(3)], tradeStatuses[random.nextInt(3)],
					random.nextInt(1001), random.nextInt(101), "N",
					LocalDateTime.now().minusDays(random.nextInt(365)),
					LocalDateTime.now().minusDays(random.nextInt(365))
				});

				createdCount++;

				if (batchData.size() >= DummyDataGenerator.BATCH_SIZE) {
					dataGenerator.saveProductBatch(batchData);
				}
			}
		}

		if (!batchData.isEmpty()) {
			dataGenerator.saveProductBatch(batchData);
		}

		long duration = System.currentTimeMillis() - startTime;
		log.info("상품 생성 완료: {}개 ({}초)", createdCount, duration / 1000);
	}
}