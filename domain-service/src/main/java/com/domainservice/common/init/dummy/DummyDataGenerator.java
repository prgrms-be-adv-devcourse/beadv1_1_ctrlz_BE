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

	// 카테고리 ID 매핑 캐시
	private Map<String, String> categoryNameToIdMap;

	// Random 객체 재사용 (성능 개선)
	private final Random random = new Random();

	// 배치 크기 (메모리 제약 시 1000, 여유 있으면 5000)
	private static final int BATCH_SIZE = 1000;

	@Transactional
	public void generateAllDummyData(int productCount) {
		long startTime = System.currentTimeMillis();
		log.info("=== 더미 데이터 생성 시작: {}개 ===", productCount);

		// Step 0: 카테고리와 태그 초기화
		generateCategories();
		generateTags();

		// Step 1: 파일에서 데이터 로드 및 카테고리 매핑
		loadTemplateData();
		loadCategoryMapping();

		// Step 2: 상품명 데이터 준비 (임시 테이블 대신 메모리 사용)
		List<ProductNameData> productNamesList = prepareProductNames();

		// Step 3: 상품 데이터 대량 생성
		generateProductPosts(productCount, productNamesList);

		// Step 4: 이미지 및 관계 데이터 생성
		generateImagesAndRelations(productCount);

		// Step 5: 상품-태그 관계 생성
		generateProductPostTags();

		long duration = System.currentTimeMillis() - startTime;
		log.info("=== 더미 데이터 생성 완료: {}초 (평균 {}건/초) ===",
			duration / 1000,
			productCount * 1000 / duration);
	}

	public void generateCategories() {
		List<String> categoryNames = dataLoader.loadLines("dummy/categories.txt");
		List<String> existingCategories = jdbcTemplate.queryForList(
			"SELECT name FROM category WHERE delete_status = 'N'",
			String.class
		);

		List<Object[]> batchData = new ArrayList<>();
		LocalDateTime now = LocalDateTime.now();

		for (String categoryName : categoryNames) {
			if (categoryName.trim().isEmpty() || categoryName.trim().startsWith("#")) {
				continue;
			}
			if (existingCategories.contains(categoryName)) {
				continue;
			}

			batchData.add(new Object[] {
				UuidCreator.getTimeOrderedEpoch().toString(),
				categoryName, "N", now, now
			});
		}

		if (!batchData.isEmpty()) {
			jdbcTemplate.batchUpdate(
				"INSERT INTO category (id, name, delete_status, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
				batchData
			);
			log.info("카테고리 생성: {}개", batchData.size());
		}
	}

	public void generateTags() {
		List<String> tagNames = dataLoader.loadLines("dummy/tags.txt");
		List<String> existingTags = jdbcTemplate.queryForList(
			"SELECT name FROM tag WHERE delete_status = 'N'",
			String.class
		);

		List<Object[]> batchData = new ArrayList<>();
		LocalDateTime now = LocalDateTime.now();

		for (String tagName : tagNames) {
			if (tagName.trim().isEmpty() || tagName.trim().startsWith("#")) {
				continue;
			}
			if (existingTags.contains(tagName)) {
				continue;
			}

			batchData.add(new Object[] {
				UuidCreator.getTimeOrderedEpoch().toString(),
				tagName, "N", now, now
			});
		}

		if (!batchData.isEmpty()) {
			jdbcTemplate.batchUpdate(
				"INSERT INTO tag (id, name, delete_status, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
				batchData
			);
			log.info("태그 생성: {}개", batchData.size());
		}
	}

	private void loadTemplateData() {
		titlePrefixes = dataLoader.loadLines("dummy/products/title-prefixes.txt");
		productNames = dataLoader.loadProductNames("dummy/products/product-names.csv");
		conditionWords = dataLoader.loadLines("dummy/products/condition-words.txt");
		descriptionPrefixes = dataLoader.loadLines("dummy/products/descriptions-prefixes.txt");
		descriptions = dataLoader.loadLines("dummy/products/descriptions.txt");
	}

	private void loadCategoryMapping() {
		categoryNameToIdMap = new HashMap<>();
		List<Map<String, Object>> categories = jdbcTemplate.queryForList(
			"SELECT id, name FROM category WHERE delete_status = 'N'"
		);
		for (Map<String, Object> row : categories) {
			categoryNameToIdMap.put((String)row.get("name"), (String)row.get("id"));
		}
	}

	private record ProductNameData(String categoryId, String name) {}

	/**
	 * 임시 테이블 대신 메모리에서 직접 상품명 리스트 준비
	 */
	private List<ProductNameData> prepareProductNames() {
		List<ProductNameData> result = new ArrayList<>();

		for (Map.Entry<String, List<String>> entry : productNames.entrySet()) {
			String categoryId = categoryNameToIdMap.get(entry.getKey());
			if (categoryId != null) {
				for (String name : entry.getValue()) {
					result.add(new ProductNameData(categoryId, name));
				}
			}
		}

		log.info("상품명 데이터 준비: {}개", result.size());
		return result;
	}

	public void generateProductPosts(int count, List<ProductNameData> productNamesList) {
		if (productNamesList.isEmpty()) {
			log.error("상품명 데이터 없음");
			return;
		}

		long startTime = System.currentTimeMillis();
		List<Object[]> batchData = new ArrayList<>(BATCH_SIZE);

		int productsPerUser = 5;
		int totalUsers = (int)Math.ceil((double)count / productsPerUser);
		int createdCount = 0;

		// 자주 사용하는 값들 미리 계산
		int productNamesSize = productNamesList.size();
		int titlePrefixesSize = titlePrefixes.size();
		int conditionWordsSize = conditionWords.size();
		int descriptionPrefixesSize = descriptionPrefixes.size();
		int descriptionsSize = descriptions.size();
		String[] statuses = {"NEW", "GOOD", "FAIR"};
		String[] tradeStatuses = {"SELLING", "PROCESSING", "SOLDOUT"};

		for (int u = 0; u < totalUsers; u++) {
			String userId = UuidCreator.getTimeOrderedEpoch().toString();

			for (int p = 0; p < productsPerUser && createdCount < count; p++) {
				ProductNameData product = productNamesList.get(random.nextInt(productNamesSize));
				String productId = UuidCreator.getTimeOrderedEpoch().toString();

				// StringBuilder 사용으로 String 연결 최적화
				String title = product.name() + " " +
					conditionWords.get(random.nextInt(conditionWordsSize)) + " " +
					titlePrefixes.get(random.nextInt(titlePrefixesSize));

				String description = product.name() + " " +
					descriptionPrefixes.get(random.nextInt(descriptionPrefixesSize)) + " " +
					descriptions.get(random.nextInt(descriptionsSize));

				batchData.add(new Object[] {
					productId,
					userId,
					product.categoryId(),
					title,
					product.name(),
					(random.nextInt(2001) * 1000) + 500000, // price
					description,
					statuses[random.nextInt(3)],
					tradeStatuses[random.nextInt(3)],
					random.nextInt(1001), // viewCount
					random.nextInt(101),  // likedCount
					"N",
					LocalDateTime.now().minusDays(random.nextInt(365)),
					LocalDateTime.now().minusDays(random.nextInt(365))
				});

				createdCount++;

				if (batchData.size() >= BATCH_SIZE) {
					saveProductBatch(batchData);
				}
			}
		}

		if (!batchData.isEmpty()) {
			saveProductBatch(batchData);
		}

		long duration = System.currentTimeMillis() - startTime;
		log.info("상품 생성 완료: {}개 ({}초)", createdCount, duration / 1000);
	}

	private void saveProductBatch(List<Object[]> batchData) {
		jdbcTemplate.batchUpdate(
			"INSERT INTO product_post (id, user_id, category_id, title, name, price, description, status, trade_status, view_count, liked_count, delete_status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
			batchData
		);
		batchData.clear();
	}

	public void generateImagesAndRelations(int productCount) {
		String selectSql = """
			SELECT p.id, p.created_at
			FROM product_post p
			WHERE NOT EXISTS (SELECT 1 FROM product_post_images ppi WHERE ppi.product_post_id = p.id)
			LIMIT ?
			""";

		List<Map<String, Object>> products = jdbcTemplate.queryForList(selectSql, productCount);

		if (products.isEmpty()) {
			return;
		}

		long startTime = System.currentTimeMillis();
		List<Object[]> imageBatchData = new ArrayList<>(BATCH_SIZE);
		List<Object[]> relationBatchData = new ArrayList<>(BATCH_SIZE);

		for (Map<String, Object> product : products) {
			String productId = (String)product.get("id");
			LocalDateTime createdAt = (LocalDateTime)product.get("created_at");
			String imageId = UuidCreator.getTimeOrderedEpoch().toString();

			// StringBuilder 사용으로 문자열 연결 최적화
			String filePrefix = productId.substring(0, 8);
			StringBuilder urlBuilder = new StringBuilder(60);

			imageBatchData.add(new Object[] {
				imageId,
				"product_" + filePrefix + ".jpg",
				"product_" + filePrefix + ".webp",
				urlBuilder.append("https://s3.amazonaws.com/products/").append(filePrefix).append(".webp").toString(),
				"products/" + filePrefix + ".webp",
				1000000 + random.nextInt(2000000),
				"image/jpeg",
				250000 + random.nextInt(500000),
				"WEBP",
				"PRODUCT",
				"N",
				createdAt,
				createdAt
			});

			relationBatchData.add(new Object[] {
				UuidCreator.getTimeOrderedEpoch().toString(),
				productId,
				imageId,
				0,
				true,
				"N",
				createdAt,
				createdAt
			});

			if (imageBatchData.size() >= BATCH_SIZE) {
				saveImageBatch(imageBatchData);
				saveProductImageBatch(relationBatchData);
			}
		}

		if (!imageBatchData.isEmpty()) {
			saveImageBatch(imageBatchData);
			saveProductImageBatch(relationBatchData);
		}

		long duration = System.currentTimeMillis() - startTime;
		log.info("이미지/관계 생성 완료: {}개 ({}초)", products.size(), duration / 1000);
	}

	private void saveImageBatch(List<Object[]> batchData) {
		jdbcTemplate.batchUpdate(
			"INSERT INTO images (id, original_file_name, stored_file_name, s3_url, s3_key, original_file_size, original_content_type, compressed_file_size, converted_content_type, image_target, delete_status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
			batchData
		);
		batchData.clear();
	}

	private void saveProductImageBatch(List<Object[]> batchData) {
		jdbcTemplate.batchUpdate(
			"INSERT INTO product_post_images (id, product_post_id, image_id, display_order, is_primary, delete_status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
			batchData
		);
		batchData.clear();
	}

	public void generateProductPostTags() {
		List<String> tagIds = jdbcTemplate.queryForList(
			"SELECT id FROM tag WHERE delete_status = 'N'",
			String.class
		);

		if (tagIds.isEmpty()) {
			return;
		}

		List<String> productIds = jdbcTemplate.queryForList(
			"SELECT id FROM product_post p WHERE NOT EXISTS (SELECT 1 FROM product_post_tag ppt WHERE ppt.product_post_id = p.id)",
			String.class
		);

		if (productIds.isEmpty()) {
			return;
		}

		long startTime = System.currentTimeMillis();
		List<Object[]> batchData = new ArrayList<>(BATCH_SIZE);
		int tagIdsSize = tagIds.size();

		for (String productId : productIds) {
			if (random.nextDouble() >= 0.8) {
				continue;
			}

			int tagCount = random.nextInt(3) + 1;
			List<String> selectedTags = new ArrayList<>(tagCount);

			for (int i = 0; i < tagCount; i++) {
				String tagId = tagIds.get(random.nextInt(tagIdsSize));
				if (!selectedTags.contains(tagId)) {
					selectedTags.add(tagId);
					batchData.add(new Object[] {productId, tagId});

					if (batchData.size() >= BATCH_SIZE) {
						saveProductTagBatch(batchData);
					}
				}
			}
		}

		if (!batchData.isEmpty()) {
			saveProductTagBatch(batchData);
		}

		long duration = System.currentTimeMillis() - startTime;
		log.info("태그 관계 생성 완료 ({}초)", duration / 1000);
	}

	private void saveProductTagBatch(List<Object[]> batchData) {
		jdbcTemplate.batchUpdate(
			"INSERT INTO product_post_tag (product_post_id, tag_id) VALUES (?, ?)",
			batchData
		);
		batchData.clear();
	}
}