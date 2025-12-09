package com.domainservice.common.init.dummy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DummyDataGenerator {

	private final JdbcTemplate jdbcTemplate;
	private final DummyDataLoader dataLoader;

	// 캐시된 데이터
	private List<String> titlePrefixes;
	private Map<String, List<String>> productNames;
	private List<String> conditionWords;
	private List<String> descriptionPrefixes;
	private List<String> descriptions;

	@Transactional
	public void generateAllDummyData(int productCount) {
		long startTime = System.currentTimeMillis();

		log.info("=== 더미 데이터 생성 시작 ===");

		// Step 1: 파일에서 데이터 로드
		loadTemplateData();

		// Step 2: 값 테이블 생성
		createValueTables();

		// Step 3: 상품 데이터 대량 생성
		generateProductPosts(productCount);

		// Step 4: 이미지 데이터 생성
		generateImages(productCount);

		// Step 5: 상품-이미지 관계 생성
		generateProductPostImages();

		// Step 6: 상품-태그 관계 생성
		generateProductPostTags();

		long duration = System.currentTimeMillis() - startTime;
		log.info("=== 더미 데이터 생성 완료 ===");
		log.info("소요 시간: {}초", duration / 1000);
	}

	/**
	 * 파일에서 템플릿 데이터 로드
	 */
	private void loadTemplateData() {
		titlePrefixes = dataLoader.loadLines("dummy/title-prefixes.txt");
		productNames = dataLoader.loadProductNames("dummy/product-names.csv");
		conditionWords = dataLoader.loadLines("dummy/condition-words.txt");
		descriptionPrefixes = dataLoader.loadLines("dummy/descriptions-prefixes.txt");
		descriptions = dataLoader.loadLines("dummy/descriptions.txt");
	}

	/**
	 * 로드된 데이터로 임시 테이블 생성
	 */
	private void createValueTables() {
		// 1. 제목 접두사 테이블 생성
		jdbcTemplate.execute("""
			CREATE TEMPORARY TABLE IF NOT EXISTS title_prefixes (
				id INT AUTO_INCREMENT PRIMARY KEY,
				prefix VARCHAR(50)
			)
			""");

		jdbcTemplate.batchUpdate(
			"INSERT INTO title_prefixes (prefix) VALUES (?)",
			titlePrefixes,
			titlePrefixes.size(),
			(ps, prefix) -> ps.setString(1, prefix)
		);

		// 2. 상품명 테이블 생성
		jdbcTemplate.execute("""
			CREATE TEMPORARY TABLE IF NOT EXISTS product_names (
				id INT AUTO_INCREMENT PRIMARY KEY,
				category_id VARCHAR(255),
				name VARCHAR(200)
			)
			""");

		// 카테고리명으로 로드된 데이터를 카테고리 ID로 변환하여 삽입
		for (Map.Entry<String, List<String>> entry : productNames.entrySet()) {
			String categoryName = entry.getKey();
			List<String> names = entry.getValue();

			try {
				// DB에서 카테고리명으로 ID 조회
				List<String> categoryIds = jdbcTemplate.query(
					"SELECT id FROM category WHERE name = ?",
					(rs, rowNum) -> rs.getString("id"),
					categoryName
				);

				if (categoryIds.isEmpty()) {
					log.warn("⚠️  카테고리를 찾을 수 없음: '{}'", categoryName);
					continue;
				}

				String categoryId = categoryIds.get(0);

				jdbcTemplate.batchUpdate(
					"INSERT INTO product_names (category_id, name) VALUES (?, ?)",
					names,
					names.size(),
					(ps, name) -> {
						ps.setString(1, categoryId);
						ps.setString(2, name);
					}
				);

				log.info("카테고리 '{}': {}개 상품명 로드", categoryName, names.size());

			} catch (Exception e) {
				log.error("카테고리 '{}' 처리 중 오류: {}", categoryName, e.getMessage());
			}
		}

		// 3. 상태 키워드 테이블 생성
		jdbcTemplate.execute("""
			CREATE TEMPORARY TABLE IF NOT EXISTS condition_words (
				id INT AUTO_INCREMENT PRIMARY KEY,
				word VARCHAR(50)
			)
			""");

		jdbcTemplate.batchUpdate(
			"INSERT INTO condition_words (word) VALUES (?)",
			conditionWords,
			conditionWords.size(),
			(ps, word) -> ps.setString(1, word)
		);

		// 4. 설명 접두사 테이블 생성
		jdbcTemplate.execute("""
			CREATE TEMPORARY TABLE IF NOT EXISTS description_prefixes (
				id INT AUTO_INCREMENT PRIMARY KEY,
				prefix TEXT
			)
			""");

		jdbcTemplate.batchUpdate(
			"INSERT INTO description_prefixes (prefix) VALUES (?)",
			descriptionPrefixes,
			descriptionPrefixes.size(),
			(ps, prefix) -> ps.setString(1, prefix)
		);

		// 5. 설명 템플릿 테이블 생성
		jdbcTemplate.execute("""
			CREATE TEMPORARY TABLE IF NOT EXISTS desc_templates (
				id INT AUTO_INCREMENT PRIMARY KEY,
				template TEXT
			)
			""");

		jdbcTemplate.batchUpdate(
			"INSERT INTO desc_templates (template) VALUES (?)",
			descriptions,
			descriptions.size(),
			(ps, desc) -> ps.setString(1, desc)
		);

		log.info("값 테이블 생성 완료");
	}

	private record ProductNameData(String categoryId, String name) {
	}

	@Transactional
	public void generateProductPosts(int count) {
		log.info("상품 데이터 생성 시작: {}개", count);

		// 1. 모든 상품명을 메모리에 로드
		List<ProductNameData> productNamesList = jdbcTemplate.query(
			"SELECT category_id, name FROM product_names",
			(rs, rowNum) -> new ProductNameData(
				rs.getString("category_id"),
				rs.getString("name")
			)
		);

		if (productNamesList.isEmpty()) {
			log.error("product_names 테이블이 비어있습니다!");
			return;
		}

		log.info("사용 가능한 상품명: {}개", productNamesList.size());

		// 2. 배치로 생성
		int batchSize = 1000;
		List<Object[]> batchData = new ArrayList<>();

		Random random = new Random();

		for (int i = 0; i < count; i++) {
			// 랜덤하게 상품 선택
			ProductNameData product = productNamesList.get(random.nextInt(productNamesList.size()));

			String productId = UUID.randomUUID().toString() + "-product";
			String userId = UUID.randomUUID().toString() + "-user";
			String categoryId = product.categoryId();
			String productName = product.name();

			// 랜덤 값 생성
			String titlePrefix = titlePrefixes.get(random.nextInt(titlePrefixes.size()));
			String conditionWord = conditionWords.get(random.nextInt(conditionWords.size()));
			String descriptionPrefix = descriptionPrefixes.get(random.nextInt(descriptionPrefixes.size()));
			String descriptionTemplate = descriptions.get(random.nextInt(descriptions.size()));

			String title = productName + " " + conditionWord + " " + titlePrefix;
			int price = (random.nextInt(2001) * 1000) + 500000;
			String description = productName + " " + descriptionPrefix + " " + descriptionTemplate;
			String status = new String[] {"NEW", "GOOD", "FAIR"}[random.nextInt(3)];
			String tradeStatus = new String[] {"SELLING", "PROCESSING", "SOLDOUT"}[random.nextInt(3)];
			int viewCount = random.nextInt(1001);
			int likedCount = random.nextInt(101);

			LocalDateTime createdAt = LocalDateTime.now().minusDays(random.nextInt(365));

			batchData.add(new Object[] {
				productId, userId, categoryId, title, productName, price, description,
				status, tradeStatus, viewCount, likedCount, "N", createdAt, createdAt
			});

			// 배치 실행
			if (batchData.size() >= batchSize || i == count - 1) {
				jdbcTemplate.batchUpdate(
					"""
						INSERT INTO product_post 
						(id, user_id, category_id, title, name, price, description, 
						 status, trade_status, view_count, liked_count, delete_status, 
						 created_at, updated_at)
						VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
						""",
					batchData
				);

				batchData.clear();
			}
		}

		log.info("{}개의 상품 데이터 생성 완료", count);
	}

	/**
	 * Step 3: 이미지 데이터 생성
	 */
	@Transactional
	public void generateImages(int productCount) {
		String sql = """
			INSERT INTO images 
			(id, original_file_name, stored_file_name, s3_url, s3_key, 
			 original_file_size, original_content_type, compressed_file_size, 
			 converted_content_type, image_target, delete_status, created_at, updated_at)
			SELECT 
				CONCAT(UUID(), '-images'),
				CONCAT('product_', SUBSTRING(p.id, 1, 8), '.jpg'),
				CONCAT('product_', SUBSTRING(p.id, 1, 8), '.webp'),
				CONCAT('https://s3.amazonaws.com/products/', SUBSTRING(p.id, 1, 8), '.webp'),
				CONCAT('products/', SUBSTRING(p.id, 1, 8), '.webp'),
				FLOOR(1000000 + RAND() * 2000000),
				'image/jpeg',
				FLOOR(250000 + RAND() * 500000),
				'WEBP',
				'PRODUCT',
				'N',
				p.created_at,
				p.created_at
			FROM product_post p
			WHERE NOT EXISTS (
				SELECT 1 FROM images i 
				WHERE i.s3_key = CONCAT('products/', SUBSTRING(p.id, 1, 8), '.webp')
			)
			AND p.id NOT LIKE '01939a8c-40__-7000-8000-000000000001-product'
			LIMIT ?
			""";

		jdbcTemplate.update(sql, productCount);
		log.info("{}개의 이미지 데이터 생성 완료", productCount);
	}

	/**
	 * Step 4: 상품-이미지 관계 생성
	 */
	@Transactional
	public void generateProductPostImages() {
		String sql = """
			INSERT INTO product_post_images 
			(id, product_post_id, image_id, display_order, is_primary, 
			 delete_status, created_at, updated_at)
			SELECT 
				CONCAT(UUID(), '-product_post_images'),
				p.id,
				i.id,
				0,
				true,
				'N',
				p.created_at,
				p.created_at
			FROM product_post p
			INNER JOIN images i ON i.s3_key = CONCAT('products/', SUBSTRING(p.id, 1, 8), '.webp')
			WHERE NOT EXISTS (
				SELECT 1 FROM product_post_images ppi 
				WHERE ppi.product_post_id = p.id
			)
			AND p.id NOT LIKE '01939a8c-40__-7000-8000-000000000001-product'
			""";

		int count = jdbcTemplate.update(sql);
		log.info("{}개의 상품-이미지 관계 생성 완료", count);
	}

	/**
	 * Step 5: 상품-태그 관계 생성
	 */
	@Transactional
	public void generateProductPostTags() {
		String sql = """
			INSERT INTO product_post_tag (product_post_id, tag_id)
			SELECT DISTINCT
				p.id,
				(SELECT id FROM tag ORDER BY RAND() LIMIT 1)
			FROM product_post p
			CROSS JOIN (
				SELECT 1 AS tag_num 
				UNION ALL SELECT 2 
				UNION ALL SELECT 3
			) AS tag_count
			WHERE NOT EXISTS (
				SELECT 1 FROM product_post_tag ppt 
				WHERE ppt.product_post_id = p.id 
				HAVING COUNT(*) >= 3
			)
			AND p.id NOT LIKE '01939a8c-40__-7000-8000-000000000001-product'
			AND RAND() < 0.8
			""";

		int count = jdbcTemplate.update(sql);
		log.info("{}개의 상품-태그 관계 생성 완료", count);
	}

}