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

	// 카테고리 ID 매핑 (메모리 캐시)
	private Map<String, String> categoryNameToIdMap;

	@Transactional
	public void generateAllDummyData(int productCount) {
		long startTime = System.currentTimeMillis();

		log.info("=== 더미 데이터 생성 시작 ===");

		// Step 1: 파일에서 데이터 로드
		loadTemplateData();

		// Step 2: 카테고리 매핑 로드
		loadCategoryMapping();

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

		log.info("템플릿 데이터 로드 완료 - 제목접두사: {}, 상품명 카테고리: {}, 상태단어: {}, 설명접두사: {}, 설명: {}",
			titlePrefixes.size(), productNames.size(), conditionWords.size(),
			descriptionPrefixes.size(), descriptions.size());
	}

	/**
	 * 카테고리명 -> ID 매핑을 메모리에 로드
	 */
	private void loadCategoryMapping() {
		categoryNameToIdMap = new HashMap<>();

		List<Map<String, Object>> categories = jdbcTemplate.queryForList(
			"SELECT id, name FROM category"
		);

		for (Map<String, Object> row : categories) {
			String id = (String) row.get("id");
			String name = (String) row.get("name");
			categoryNameToIdMap.put(name, id);
		}

		log.info("카테고리 매핑 로드 완료 - {}개 카테고리", categoryNameToIdMap.size());
	}

	private record ProductNameData(String categoryId, String name) {
	}

	@Transactional
	public void generateProductPosts(int count) {
		// 파일에서 로드한 상품명을 메모리에서 카테고리 ID와 매핑
		List<ProductNameData> productNamesList = new ArrayList<>();

		for (Map.Entry<String, List<String>> entry : productNames.entrySet()) {
			String categoryName = entry.getKey();
			String categoryId = categoryNameToIdMap.get(categoryName);

			if (categoryId == null) {
				log.warn("카테고리를 찾을 수 없음: {}", categoryName);
				continue;
			}

			for (String productName : entry.getValue()) {
				productNamesList.add(new ProductNameData(categoryId, productName));
			}
		}

		if (productNamesList.isEmpty()) {
			log.error("매핑된 상품명이 없습니다.");
			return;
		}

		log.info("상품명 데이터 준비 완료 - {}개", productNamesList.size());

		// 배치 설정
		int batchSize = 1000;
		List<Object[]> batchData = new ArrayList<>();
		Random random = new Random();

		// 유저당 생성 로직 설정
		int productsPerUser = 5;
		int totalUsers = (int)Math.ceil((double)count / productsPerUser);
		int createdCount = 0;

		for (int u = 0; u < totalUsers; u++) {
			// UUIDv7로 사용자 ID 생성
			String userId = UuidCreator.getTimeOrderedEpoch().toString();

			for (int p = 0; p < productsPerUser && createdCount < count; p++) {
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

				batchData.add(new Object[] {
					productId, userId, categoryId, title, productName, price, description,
					status, tradeStatus, viewCount, likedCount, "N", createdAt, createdAt
				});

				createdCount++;

				if (batchData.size() >= batchSize) {
					saveBatch(batchData);
					log.info("상품 생성 진행 중: {}/{}", createdCount, count);
				}
			}
		}

		if (!batchData.isEmpty()) {
			saveBatch(batchData);
		}

		log.info("상품 생성 완료: {}개", createdCount);
	}

	private void saveBatch(List<Object[]> batchData) {
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
	 * 이미지 데이터 생성 (UUIDv7 적용)
	 */
	@Transactional
	public void generateImages(int productCount) {
		// 먼저 이미지가 없는 상품 조회
		String selectSql = """
			SELECT 
				p.id,
				p.created_at,
				CONCAT('product_', SUBSTRING(p.id, 1, 8), '.jpg') as original_file_name,
				CONCAT('product_', SUBSTRING(p.id, 1, 8), '.webp') as stored_file_name,
				CONCAT('https://s3.amazonaws.com/products/', SUBSTRING(p.id, 1, 8), '.webp') as s3_url,
				CONCAT('products/', SUBSTRING(p.id, 1, 8), '.webp') as s3_key
			FROM product_post p
			WHERE NOT EXISTS (
				SELECT 1 FROM images i 
				WHERE i.s3_key = CONCAT('products/', SUBSTRING(p.id, 1, 8), '.webp')
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
		List<Object[]> batchData = new ArrayList<>();
		Random random = new Random();

		for (Map<String, Object> product : products) {
			// UUIDv7로 이미지 ID 생성
			String imageId = UuidCreator.getTimeOrderedEpoch().toString();
			String originalFileName = (String) product.get("original_file_name");
			String storedFileName = (String) product.get("stored_file_name");
			String s3Url = (String) product.get("s3_url");
			String s3Key = (String) product.get("s3_key");
			LocalDateTime createdAt = (LocalDateTime) product.get("created_at");

			long originalFileSize = 1000000 + random.nextInt(2000000);
			long compressedFileSize = 250000 + random.nextInt(500000);

			batchData.add(new Object[] {
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

			if (batchData.size() >= batchSize) {
				saveImageBatch(batchData);
			}
		}

		if (!batchData.isEmpty()) {
			saveImageBatch(batchData);
		}

		log.info("{}개의 이미지 생성 완료", products.size());
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

	/**
	 * 상품-이미지 관계 생성 (UUIDv7 적용)
	 */
	@Transactional
	public void generateProductPostImages() {
		// 먼저 관계가 없는 상품-이미지 쌍 조회
		String selectSql = """
			SELECT 
				p.id as product_id,
				i.id as image_id,
				p.created_at
			FROM product_post p
			INNER JOIN images i ON i.s3_key = CONCAT('products/', SUBSTRING(p.id, 1, 8), '.webp')
			WHERE NOT EXISTS (
				SELECT 1 FROM product_post_images ppi 
				WHERE ppi.product_post_id = p.id
			)
			""";

		List<Map<String, Object>> relations = jdbcTemplate.queryForList(selectSql);

		if (relations.isEmpty()) {
			log.info("생성할 상품-이미지 관계가 없습니다.");
			return;
		}

		// 배치 데이터 준비
		int batchSize = 1000;
		List<Object[]> batchData = new ArrayList<>();

		for (Map<String, Object> relation : relations) {
			// UUIDv7로 관계 ID 생성
			String relationId = UuidCreator.getTimeOrderedEpoch().toString();
			String productId = (String) relation.get("product_id");
			String imageId = (String) relation.get("image_id");
			LocalDateTime createdAt = (LocalDateTime) relation.get("created_at");

			batchData.add(new Object[] {
				relationId,
				productId,
				imageId,
				0,        // display_order
				true,     // is_primary
				"N",
				createdAt,
				createdAt
			});

			if (batchData.size() >= batchSize) {
				saveProductImageBatch(batchData);
			}
		}

		if (!batchData.isEmpty()) {
			saveProductImageBatch(batchData);
		}

		log.info("{}개의 상품-이미지 관계 생성 완료", relations.size());
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
		List<String> tagIds = jdbcTemplate.queryForList("SELECT id FROM tag", String.class);

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
			// 80% 확률로 태그 추가
			if (random.nextDouble() >= 0.8) {
				continue;
			}

			// 각 상품에 1~3개의 랜덤 태그 추가
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