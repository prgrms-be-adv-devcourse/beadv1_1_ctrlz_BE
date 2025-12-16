package com.domainservice.common.init.dummy.service;

import com.domainservice.domain.search.model.entity.dto.document.ProductPostDocumentEntity;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 더미 데이터 생성 공통 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DummyDataGenerator {

	private final JdbcTemplate jdbcTemplate;
	private final ElasticsearchOperations elasticsearchOperations;
	private final DummyDataLoader dataLoader;
	private final Random random = new Random();

	public static final int BATCH_SIZE = 1000;

	/**
	 * 카테고리 생성
	 */
	public void generateCategories() {
		List<String> categoryNames = dataLoader.loadLines("dummy/categories.txt");
		List<String> existingCategories = jdbcTemplate.queryForList(
				"SELECT name FROM category WHERE delete_status = 'N'",
				String.class);

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
					batchData);
			log.info("카테고리 생성: {}개", batchData.size());
		}
	}

	/**
	 * 태그 생성
	 */
	public void generateTags() {
		List<String> tagNames = dataLoader.loadLines("dummy/tags.txt");
		List<String> existingTags = jdbcTemplate.queryForList(
				"SELECT name FROM tag WHERE delete_status = 'N'",
				String.class);

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
					batchData);
			log.info("태그 생성: {}개", batchData.size());
		}
	}

	public record TemplateData(
			List<String> titlePrefixes,
			Map<String, List<String>> productNames,
			List<String> conditionWords,
			List<String> descriptionPrefixes,
			List<String> descriptions) {
	}

	public TemplateData loadTemplateData() {
		List<String> titlePrefixes = dataLoader.loadLines("dummy/products/title-prefixes.txt");
		Map<String, List<String>> productNames = dataLoader.loadProductNames("dummy/products/product-names.csv");
		List<String> conditionWords = dataLoader.loadLines("dummy/products/condition-words.txt");
		List<String> descriptionPrefixes = dataLoader.loadLines("dummy/products/descriptions-prefixes.txt");
		List<String> descriptions = dataLoader.loadLines("dummy/products/descriptions.txt");

		return new TemplateData(titlePrefixes, productNames, conditionWords, descriptionPrefixes, descriptions);
	}

	/**
	 * 카테고리 매핑 로드
	 */
	public Map<String, String> loadCategoryMapping() {
		Map<String, String> categoryNameToIdMap = new HashMap<>();
		List<Map<String, Object>> categories = jdbcTemplate.queryForList(
				"SELECT id, name FROM category WHERE delete_status = 'N'");
		for (Map<String, Object> row : categories) {
			categoryNameToIdMap.put((String) row.get("name"), (String) row.get("id"));
		}
		return categoryNameToIdMap;
	}

	/**
	 * 이미지 및 관계 생성
	 */
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
			String productId = (String) product.get("id");
			LocalDateTime createdAt = (LocalDateTime) product.get("created_at");
			String imageId = UuidCreator.getTimeOrderedEpoch().toString();
			String filePrefix = productId.substring(0, 8);

			imageBatchData.add(new Object[] {
					imageId,
					"product_" + filePrefix + ".jpg",
					"product_" + filePrefix + ".webp",
					"https://s3.amazonaws.com/products/" + filePrefix + ".webp",
					"products/" + filePrefix + ".webp",
					1000000 + random.nextInt(2000000),
					"image/jpeg",
					250000 + random.nextInt(500000),
					"WEBP", "PRODUCT", "N", createdAt, createdAt
			});

			relationBatchData.add(new Object[] {
					UuidCreator.getTimeOrderedEpoch().toString(),
					productId, imageId, 0, true, "N", createdAt, createdAt
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
		log.info("상품 이미지 생성 및 상품/이미지 관계 생성 완료: {}개 ({}초)", products.size(), duration / 1000);
	}

	/**
	 * 상품-태그 관계 생성
	 */
	public void generateProductPostTags() {
		List<String> tagIds = jdbcTemplate.queryForList(
				"SELECT id FROM tag WHERE delete_status = 'N'",
				String.class);
		if (tagIds.isEmpty()) {
			return;
		}

		List<String> productIds = jdbcTemplate.queryForList(
				"SELECT id FROM product_post p WHERE NOT EXISTS (SELECT 1 FROM product_post_tag ppt WHERE ppt.product_post_id = p.id)",
				String.class);
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
					batchData.add(new Object[] { productId, tagId });

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
		log.info("상품/태그 관계 생성 완료 ({}초)", duration / 1000);
	}

	// Batch 저장 메서드들
	public void saveProductBatch(List<Object[]> batchData) {
		// 1. DB Bulk Insert
		jdbcTemplate.batchUpdate(
				"INSERT INTO product_post (id, user_id, category_id, title, name, price, description, status, trade_status, view_count, liked_count, delete_status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				batchData);

		// 2. ES Bulk Insert
		try {
			// 카테고리 이름 조회를 위한 ID 수집
			List<String> categoryIds = batchData.stream()
					.map(row -> (String) row[2])
					.distinct()
					.toList();

			// 카테고리 이름 조회
			Map<String, String> categoryMap = new HashMap<>();
			if (!categoryIds.isEmpty()) {
				String inSql = String.join(",", java.util.Collections.nCopies(categoryIds.size(), "?"));
				jdbcTemplate.query(
						"SELECT id, name FROM category WHERE id IN (" + inSql + ")",
						rs -> {
							categoryMap.put(rs.getString("id"), rs.getString("name"));
						},
						categoryIds.toArray());
			}

			// es Document 변환
			List<ProductPostDocumentEntity> documents = batchData.stream()
					.map(row -> ProductPostDocumentEntity.builder()
							.id((String) row[0])
							.userId((String) row[1])
							// row[2] is categoryId, mapping needed
							.categoryName(categoryMap.getOrDefault((String) row[2], "Unknown"))
							.title((String) row[3])
							.name((String) row[4])
							.price(((Number) row[5]).longValue())
							.description((String) row[6])
							.status((String) row[7])
							.tradeStatus((String) row[8])
							.viewCount(((Number) row[9]).longValue())
							.likedCount(((Number) row[10]).longValue())
							.deleteStatus((String) row[11])
							.createdAt(((LocalDateTime) row[12]).truncatedTo(ChronoUnit.MILLIS))
							.updatedAt(((LocalDateTime) row[13]).truncatedTo(ChronoUnit.MILLIS))
							.tags(new ArrayList<>()) // 초기 생성시 태그 없음
							.build())
					.toList();

			// bulk Save
			elasticsearchOperations.save(documents);

		} catch (Exception e) {
			log.error("ES Bulk Insert 중 오류 발생", e);
			// DB는 들어갔으므로 throw하지 않고 로깅만 처리하거나 정책에 따라 결정
		}

		batchData.clear();
	}

	private void saveImageBatch(List<Object[]> batchData) {
		jdbcTemplate.batchUpdate(
				"INSERT INTO images (id, original_file_name, stored_file_name, s3_url, s3_key, original_file_size, original_content_type, compressed_file_size, converted_content_type, image_target, delete_status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				batchData);
		batchData.clear();
	}

	private void saveProductImageBatch(List<Object[]> batchData) {
		jdbcTemplate.batchUpdate(
				"INSERT INTO product_post_images (id, product_post_id, image_id, display_order, is_primary, delete_status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
				batchData);
		batchData.clear();
	}

	private void saveProductTagBatch(List<Object[]> batchData) {
		jdbcTemplate.batchUpdate(
				"INSERT INTO product_post_tag (product_post_id, tag_id) VALUES (?, ?)",
				batchData);
		batchData.clear();
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public Random getRandom() {
		return random;
	}

}