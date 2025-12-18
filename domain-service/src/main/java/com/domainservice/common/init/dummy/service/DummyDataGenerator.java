package com.domainservice.common.init.dummy.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.common.event.productPost.ProductPostUpsertedEvent;
import com.domainservice.domain.search.mapper.SearchMapper;
import com.domainservice.domain.search.model.entity.dto.document.ProductPostDocumentEntity;
import com.github.f4b6a3.uuid.UuidCreator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
		// MySQL Only
		jdbcTemplate.batchUpdate(
			"INSERT INTO product_post (id, user_id, category_id, title, name, price, description, status, trade_status, view_count, liked_count, delete_status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
			batchData);
		batchData.clear();
	}

	/**
	 * DB에서 product_post를 조회하여 ES에 벌크 동기화
	 *
	 * @param limit 동기화할 최대 건수
	 */
	public void syncProductsToElasticsearch(int limit) {
		long startTime = System.currentTimeMillis();

		String sql = """
				SELECT
					p.id, p.user_id, p.title, p.name, p.price, p.description,
					p.status, p.trade_status, p.view_count, p.liked_count,
					p.delete_status, p.created_at, p.updated_at,
					c.name as category_name,
					(SELECT img.s3_url FROM product_post_images ppi2
					 JOIN images img ON ppi2.image_id = img.id
					 WHERE ppi2.product_post_id = p.id AND ppi2.is_primary = true LIMIT 1) as primary_image_url
				FROM product_post p
				LEFT JOIN category c ON p.category_id = c.id
				WHERE p.delete_status = 'N'
				LIMIT ?
				""";

		List<Map<String, Object>> products = jdbcTemplate.queryForList(sql, limit);

		if (products.isEmpty()) {
			log.info("동기화할 상품이 없습니다.");
			return;
		}

		// 상품별 태그 조회
		List<String> productIds = products.stream()
			.map(p -> (String) p.get("id"))
			.toList();

		Map<String, List<String>> productTagsMap = new HashMap<>();
		if (!productIds.isEmpty()) {
			String inSql = String.join(",", Collections.nCopies(productIds.size(), "?"));
			jdbcTemplate.query(
				"SELECT ppt.product_post_id, t.name FROM product_post_tag ppt " +
					"JOIN tag t ON ppt.tag_id = t.id WHERE ppt.product_post_id IN (" + inSql + ")",
				rs -> {
					String productId = rs.getString("product_post_id");
					String tagName = rs.getString("name");
					productTagsMap.computeIfAbsent(productId, k -> new ArrayList<>()).add(tagName);
				},
				productIds.toArray());
		}

		// ES Document 변환 및 벌크 저장
		List<ProductPostDocumentEntity> documents = new ArrayList<>(BATCH_SIZE);
		int savedCount = 0;

		for (Map<String, Object> product : products) {
			String productId = (String) product.get("id");

			// Map을 ProductPostUpsertedEvent로 변환
			ProductPostUpsertedEvent event = ProductPostUpsertedEvent.builder()
				.id(productId)
				.userId((String) product.get("user_id"))
				.categoryName((String) product.get("category_name"))
				.title((String) product.get("title"))
				.name((String) product.get("name"))
				.price(((Number) product.get("price")).longValue())
				.description((String) product.get("description"))
				.status((String) product.get("status"))
				.tradeStatus((String) product.get("trade_status"))
				.viewCount(((Number) product.get("view_count")).longValue())
				.likedCount(((Number) product.get("liked_count")).longValue())
				.deleteStatus((String) product.get("delete_status"))
				.createdAt((LocalDateTime) product.get("created_at"))
				.updatedAt((LocalDateTime) product.get("updated_at"))
				.tags(productTagsMap.getOrDefault(productId, new ArrayList<>()))
				.primaryImageUrl((String) product.get("primary_image_url"))
				.build();

			// SearchMapper를 사용하여 Document 변환
			ProductPostDocumentEntity doc = SearchMapper.toDocumentEntity(event);

			documents.add(doc);

			if (documents.size() >= BATCH_SIZE) {
				elasticsearchOperations.save(documents);
				savedCount += documents.size();
				documents.clear();
				log.info("ES 동기화 진행 중: {}건 완료", savedCount);
			}
		}

		// 남은 문서 저장
		if (!documents.isEmpty()) {
			elasticsearchOperations.save(documents);
			savedCount += documents.size();
		}

		long duration = System.currentTimeMillis() - startTime;
		log.info("ES 동기화 완료: {}건 ({}초)", savedCount, duration / 1000);
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