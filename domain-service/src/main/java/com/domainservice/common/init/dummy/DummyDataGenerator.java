package com.domainservice.common.init.dummy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.f4b6a3.uuid.UuidCreator;

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

	// 카테고리 ID 매핑 캐시 (성능 개선)
	private Map<String, String> categoryNameToIdMap;

	@Transactional
	public void generateAllDummyData(int productCount) {
		long startTime = System.currentTimeMillis();
		log.info("=== 더미 데이터 생성 시작 ===");

		// Step 0: 카테고리와 태그 초기화
		generateCategories();
		generateTags();

		// Step 1: 파일에서 데이터 로드
		loadTemplateData();

		// Step 2: 카테고리 매핑 로드 (성능 개선)
		loadCategoryMapping();

		// Step 3: 값 테이블 생성
		createValueTables();

		// Step 4: 상품 데이터 대량 생성
		generateProductPosts(productCount);

		// Step 5: 이미지 및 관계 데이터 생성 (통합)
		generateImagesAndRelations(productCount);

		// Step 6: 상품-태그 관계 생성
		generateProductPostTags();

		long duration = System.currentTimeMillis() - startTime;
		log.info("=== 더미 데이터 생성 완료 ===");
		log.info("총 소요 시간: {}초", duration / 1000);
	}

	/**
	 * 카테고리 초기화 (UUIDv7 자동 생성)
	 */
	@Transactional
	public void generateCategories() {
		List<String> categoryNames = dataLoader.loadLines("dummy/categories.txt");

		// 기존 카테고리 조회 (중복 체크용)
		List<String> existingCategories = jdbcTemplate.queryForList(
			"SELECT name FROM category WHERE delete_status = 'N'",
			String.class
		);

		// 배치 처리를 위한 데이터 준비
		List<Object[]> batchData = new ArrayList<>();
		LocalDateTime now = LocalDateTime.now();
		int skippedCount = 0;

		for (String categoryName : categoryNames) {
			// 주석이나 빈 줄 건너뛰기
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

		// 배치 삽입
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

	/**
	 * 태그 초기화 (UUIDv7, DB 레벨 처리)
	 */
	@Transactional
	public void generateTags() {
		List<String> tagNames = dataLoader.loadLines("dummy/tags.txt");

		// 기존 태그 조회 (중복 체크용)
		List<String> existingTags = jdbcTemplate.queryForList(
			"SELECT name FROM tag WHERE delete_status = 'N'",
			String.class
		);

		// 배치 처리를 위한 데이터 준비
		List<Object[]> batchData = new ArrayList<>();
		LocalDateTime now = LocalDateTime.now();
		int skippedCount = 0;

		for (String tagName : tagNames) {
			// 주석이나 빈 줄 건너뛰기
			if (tagName.trim().isEmpty() || tagName.trim().startsWith("#")) {
				continue;
			}

			if (existingTags.contains(tagName)) {
				skippedCount++;
				continue;
			}

			// UUIDv7로 ID 생성
			String tagId = UuidCreator.getTimeOrderedEpoch().toString();
			batchData.add(new Object[] {tagId, tagName, "N", now, now});
		}

		// 배치 삽입
		if (!batchData.isEmpty()) {
			jdbcTemplate.batchUpdate(
				"""
				INSERT INTO tag (id, name, delete_status, created_at, updated_at)
				VALUES (?, ?, ?, ?, ?)
				""",
				batchData
			);
			log.info("--- 태그 초기화 완료: {}개 생성, {}개 건너뜀 ---",
				batchData.size(), skippedCount);
		} else {
			log.info("--- 태그 초기화 완료: 모든 태그가 이미 존재함 ({}/{}개) ---",
				skippedCount, tagNames.size());
		}
	}

	/**
	 * 파일에서 템플릿 데이터 로드
	 */
	private void loadTemplateData() {
		titlePrefixes = dataLoader.loadLines("dummy/products/title-prefixes.txt");
		productNames = dataLoader.loadProductNames("dummy/products/product-names.csv");
		conditionWords = dataLoader.loadLines("dummy/products/condition-words.txt");
		descriptionPrefixes = dataLoader.loadLines("dummy/products/descriptions-prefixes.txt");
		descriptions = dataLoader.loadLines("dummy/products/descriptions.txt");
	}

	/**
	 * 카테고리명 -> ID 매핑을 메모리에 로드 (성능 최적화)
	 */
	private void loadCategoryMapping() {
		categoryNameToIdMap = new HashMap<>();

		List<Map<String, Object>> categories = jdbcTemplate.queryForList(
			"SELECT id, name FROM category WHERE delete_status = 'N'"
		);

		for (Map<String, Object> row : categories) {
			String id = (String)row.get("id");
			String name = (String)row.get("name");
			categoryNameToIdMap.put(name, id);
		}
	}

	/**
	 * 로드된 데이터로 임시 테이블 생성
	 */
	private void createValueTables() {
		// 제목 접두사 테이블 생성
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

		// 상품명 테이블 생성
		jdbcTemplate.execute("""
			CREATE TEMPORARY TABLE IF NOT EXISTS product_names (
				id INT AUTO_INCREMENT PRIMARY KEY,
				category_id VARCHAR(255),
				name VARCHAR(200),
				INDEX idx_category_id (category_id)
			)
			""");

		// 카테고리 매핑을 메모리에서 처리 (성능 개선)
		List<Object[]> productNameBatch = new ArrayList<>();

		for (Map.Entry<String, List<String>> entry : productNames.entrySet()) {
			String categoryName = entry.getKey();
			String categoryId = categoryNameToIdMap.get(categoryName);

			if (categoryId == null) {
				log.warn("카테고리를 찾을 수 없음: {}", categoryName);
				continue;
			}

			List<String> names = entry.getValue();
			for (String name : names) {
				productNameBatch.add(new Object[] {categoryId, name});
			}
		}

		if (!productNameBatch.isEmpty()) {
			jdbcTemplate.batchUpdate(
				"INSERT INTO product_names (category_id, name) VALUES (?, ?)",
				productNameBatch
			);
		}

		// 상태 키워드 테이블 생성
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

		// 설명 접두사 테이블 생성
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

		// 설명 템플릿 테이블 생성
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
	}

	private record ProductNameData(String categoryId, String name) {
	}

	@Transactional
	public void generateProductPosts(int count) {
		log.info("상품 데이터 생성 시작 - 목표: {}개", count);

		// 모든 상품명을 메모리에 로드
		List<ProductNameData> productNamesList = jdbcTemplate.query(
			"SELECT category_id, name FROM product_names",
			(rs, rowNum) -> new ProductNameData(
				rs.getString("category_id"),
				rs.getString("name")
			)
		);

		if (productNamesList.isEmpty()) {
			log.error("product_names 테이블이 비어있습니다.");
			return;
		}

		int batchSize = 1000;
		List<Object[]> batchData = new ArrayList<>();
		Random random = new Random();

		// 유저당 생성 로직 설정
		int productsPerUser = 5;
		int totalUsers = (int)Math.ceil((double)count / productsPerUser);
		int createdCount = 0;

		for (int u = 0; u < totalUsers; u++) {
			// UUIDv7로 유저 ID 생성
			String userId = UuidCreator.getTimeOrderedEpoch().toString();

			for (int p = 0; p < productsPerUser && createdCount < count; p++) {
				// 랜덤하게 상품 선택
				ProductNameData product = productNamesList.get(random.nextInt(productNamesList.size()));

				// UUIDv7로 상품 ID 생성
				String productId = UuidCreator.getTimeOrderedEpoch().toString();
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

				// 데이터 추가
				batchData.add(new Object[] {
					productId, userId, categoryId, title, productName, price, description,
					status, tradeStatus, viewCount, likedCount, "N", createdAt, createdAt
				});

				createdCount++;

				// 배치 사이즈가 차면 DB에 저장
				if (batchData.size() >= batchSize) {
					saveProductBatch(batchData);
				}
			}
		}

		if (!batchData.isEmpty()) {
			saveProductBatch(batchData);
		}

		log.info("상품 생성 완료: {}개", createdCount);
	}

	private void saveProductBatch(List<Object[]> batchData) {
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

	/**
	 * 이미지 및 관계 데이터 생성 (통합, 성능 최적화)
	 */
	@Transactional
	public void generateImagesAndRelations(int productCount) {

		// 이미지가 없는 상품 조회
		String selectSql = """
			SELECT 
				p.id,
				p.created_at
			FROM product_post p
			WHERE NOT EXISTS (
				SELECT 1 FROM product_post_images ppi 
				WHERE ppi.product_post_id = p.id
			)
			LIMIT ?
			""";

		List<Map<String, Object>> products = jdbcTemplate.queryForList(selectSql, productCount);

		if (products.isEmpty()) {
			log.info("생성할 이미지가 없습니다.");
			return;
		}

		// 배치 데이터 준비
		int batchSize = 1000;
		List<Object[]> imageBatchData = new ArrayList<>();
		List<Object[]> relationBatchData = new ArrayList<>();
		Random random = new Random();

		for (Map<String, Object> product : products) {
			String productId = (String)product.get("id");
			LocalDateTime createdAt = (LocalDateTime)product.get("created_at");

			// UUIDv7로 이미지 ID 생성
			String imageId = UuidCreator.getTimeOrderedEpoch().toString();

			// 파일명 생성
			String filePrefix = productId.substring(0, 8);
			String originalFileName = "product_" + filePrefix + ".jpg";
			String storedFileName = "product_" + filePrefix + ".webp";
			String s3Url = "https://s3.amazonaws.com/products/" + filePrefix + ".webp";
			String s3Key = "products/" + filePrefix + ".webp";

			long originalFileSize = 1000000 + random.nextInt(2000000);
			long compressedFileSize = 250000 + random.nextInt(500000);

			// 이미지 데이터
			imageBatchData.add(new Object[] {
				imageId,
				originalFileName,
				storedFileName,
				s3Url,
				s3Key,
				originalFileSize,
				"image/jpeg",
				compressedFileSize,
				"WEBP",
				"PRODUCT",
				"N",
				createdAt,
				createdAt
			});

			// 상품-이미지 관계 데이터
			String relationId = UuidCreator.getTimeOrderedEpoch().toString();
			relationBatchData.add(new Object[] {
				relationId,
				productId,
				imageId,
				0,        // display_order
				true,     // is_primary
				"N",
				createdAt,
				createdAt
			});

			// 배치 사이즈가 차면 저장
			if (imageBatchData.size() >= batchSize) {
				saveImageBatch(imageBatchData);
				saveProductImageBatch(relationBatchData);
			}
		}

		if (!imageBatchData.isEmpty()) {
			saveImageBatch(imageBatchData);
			saveProductImageBatch(relationBatchData);
		}

		log.info("이미지 및 관계 생성 완료: {}개", products.size());
	}

	private void saveImageBatch(List<Object[]> batchData) {
		jdbcTemplate.batchUpdate(
			"""
			INSERT INTO images 
			(id, original_file_name, stored_file_name, s3_url, s3_key, 
			 original_file_size, original_content_type, compressed_file_size, 
			 converted_content_type, image_target, delete_status, created_at, updated_at)
			VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
			""",
			batchData
		);
		batchData.clear();
	}

	private void saveProductImageBatch(List<Object[]> batchData) {
		jdbcTemplate.batchUpdate(
			"""
			INSERT INTO product_post_images 
			(id, product_post_id, image_id, display_order, is_primary, 
			 delete_status, created_at, updated_at)
			VALUES (?, ?, ?, ?, ?, ?, ?, ?)
			""",
			batchData
		);
		batchData.clear();
	}

	/**
	 * 상품-태그 관계 생성
	 */
	@Transactional
	public void generateProductPostTags() {

		// 모든 태그 ID 조회
		List<String> tagIds = jdbcTemplate.queryForList(
			"SELECT id FROM tag WHERE delete_status = 'N'",
			String.class
		);

		if (tagIds.isEmpty()) {
			log.warn("태그가 없어 상품-태그 관계를 생성할 수 없습니다.");
			return;
		}

		// 태그가 없는 상품 조회
		String selectSql = """
			SELECT id 
			FROM product_post p
			WHERE NOT EXISTS (
				SELECT 1 FROM product_post_tag ppt 
				WHERE ppt.product_post_id = p.id
			)
			""";

		List<String> productIds = jdbcTemplate.queryForList(selectSql, String.class);

		if (productIds.isEmpty()) {
			log.info("태그를 추가할 상품이 없습니다.");
			return;
		}

		// 배치 데이터 준비
		int batchSize = 1000;
		List<Object[]> batchData = new ArrayList<>();
		Random random = new Random();

		for (String productId : productIds) {

			if (random.nextDouble() >= 0.8) {
				continue;
			}

			// 각 상품에 1~3개의 랜덤 태그 추가 (중복 방지)
			int tagCount = random.nextInt(3) + 1;
			List<String> selectedTags = new ArrayList<>();

			for (int i = 0; i < tagCount; i++) {
				String tagId = tagIds.get(random.nextInt(tagIds.size()));
				if (!selectedTags.contains(tagId)) {
					selectedTags.add(tagId);

					batchData.add(new Object[] {
						productId,
						tagId
					});

					if (batchData.size() >= batchSize) {
						saveProductTagBatch(batchData);
					}
				}
			}
		}

		if (!batchData.isEmpty()) {
			saveProductTagBatch(batchData);
		}

		log.info("상품-태그 관계 생성 완료");
	}

	private void saveProductTagBatch(List<Object[]> batchData) {
		jdbcTemplate.batchUpdate(
			"INSERT INTO product_post_tag (product_post_id, tag_id) VALUES (?, ?)",
			batchData
		);
		batchData.clear();
	}
}