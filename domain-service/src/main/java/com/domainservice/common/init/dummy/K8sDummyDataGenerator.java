package com.domainservice.common.init.dummy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * K8s 환경 호환용 더미 데이터 생성기.
 * 기존 DummyDataGenerator의 CREATE TEMPORARY TABLE 로직을 제거하고
 * 순수 자바 인메모리 처리로 변경함 (GTID Consistency 문제 해결).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class K8sDummyDataGenerator {

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

        log.info("=== 더미 데이터 생성 시작 (K8s 호환 버전) ===");

        // Step 1: 파일에서 데이터 로드
        loadTemplateData();

        // Step 1.5: 카테고리 데이터 확인 및 초기화
        ensureCategoriesExist();

        // Step 2: 값 테이블 생성 (K8s 버전에서는 인메모리 처리로 생략)
        // createValueTables();

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

        titlePrefixes.clear();
        productNames.clear();
        conditionWords.clear();
        descriptionPrefixes.clear();
        descriptions.clear();

    }

    /**
     * 카테고리 데이터가 존재하는지 확인하고, 없으면 생성합니다.
     * (Prod 환경 등 DataInitializer가 동작하지 않은 경우를 대비)
     */
    private void ensureCategoriesExist() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM category", Integer.class);
        if (count != null && count > 0) {
            return;
        }

        log.info("카테고리 데이터가 없어 초기화를 시작합니다.");
        // CategoryInitializer와 동일한 로직으로 데이터 로드 (경로 주의)
        Map<String, String> categories = dataLoader.loadCategories("init/categories.csv");

        List<Object[]> batchArgs = new ArrayList<>();
        for (Map.Entry<String, String> entry : categories.entrySet()) {
            batchArgs.add(new Object[] { entry.getKey(), entry.getValue() });
        }

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(
                    "INSERT INTO category (id, name, delete_status, created_at, updated_at) VALUES (?, ?, 'N', NOW(), NOW())",
                    batchArgs);
            log.info("카테고리 {}개 생성 완료", batchArgs.size());
        }
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

    private record ProductNameData(String categoryId, String name) {
    }

    @Transactional
    public void generateProductPosts(int count) {

        // [변경점] DB Temp Table 대신 인메모리에서 데이터 조합
        List<ProductNameData> productNamesList = buildProductNamesListInMemory();

        if (productNamesList.isEmpty()) {
            log.error("상품명 데이터 로드 실패");
            return;
        }

        // 배치 설정
        int batchSize = 1000;
        List<Object[]> batchData = new ArrayList<>();
        Random random = new Random();

        // 유저당 생성 로직 설정
        int productsPerUser = 5; // 한 유저당 생성할 상품 수
        int totalUsers = (int) Math.ceil((double) count / productsPerUser);
        int createdCount = 0; // 현재까지 생성된 총 상품 수 추적

        // 이중 루프: 유저 반복 -> 상품 반복
        for (int u = 0; u < totalUsers; u++) {
            // 새로운 유저 ID 생성 (5개 상품이 공유)
            String userId = UUID.randomUUID().toString() + "-user";

            // 해당 유저의 상품 생성
            for (int p = 0; p < productsPerUser && createdCount < count; p++) {

                // 랜덤하게 상품 선택
                ProductNameData product = productNamesList.get(random.nextInt(productNamesList.size()));

                String productId = UUID.randomUUID().toString() + "-product";
                // categoryId, productName 등 추출
                String categoryId = product.categoryId();
                String productName = product.name();

                // 랜덤 값 생성 (제목, 가격, 설명 등)
                String titlePrefix = titlePrefixes.get(random.nextInt(titlePrefixes.size()));
                String conditionWord = conditionWords.get(random.nextInt(conditionWords.size()));
                String descriptionPrefix = descriptionPrefixes.get(random.nextInt(descriptionPrefixes.size()));
                String descriptionTemplate = descriptions.get(random.nextInt(descriptions.size()));

                String title = productName + " " + conditionWord + " " + titlePrefix;
                int price = (random.nextInt(2001) * 1000) + 500000;
                String description = productName + " " + descriptionPrefix + " " + descriptionTemplate;
                String status = new String[] { "NEW", "GOOD", "FAIR" }[random.nextInt(3)];
                String tradeStatus = new String[] { "SELLING", "PROCESSING", "SOLDOUT" }[random.nextInt(3)];
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
                    saveBatch(batchData);
                }
            }
        }

        // 남은 데이터가 있다면 마지막으로 저장
        if (!batchData.isEmpty()) {
            saveBatch(batchData);
        }
    }

    /**
     * [신규] 카테고리 정보와 상품명 맵을 이용하여 인메모리 리스트 생성
     */
    private List<ProductNameData> buildProductNamesListInMemory() {
        // 1. 카테고리 ID - 이름 매핑 조회
        Map<String, String> categoryNameToIdMap = new HashMap<>();

        jdbcTemplate.query("SELECT id, name FROM category", (rs) -> {
            categoryNameToIdMap.put(rs.getString("name"), rs.getString("id"));
        });

        List<ProductNameData> result = new ArrayList<>();

        // 2. productNames 맵 순회 (Key: 카테고리명, Value: 상품명 리스트)
        for (Map.Entry<String, List<String>> entry : productNames.entrySet()) {
            String categoryName = entry.getKey();
            String categoryId = categoryNameToIdMap.get(categoryName);

            // DB에 없는 카테고리면 스킵
            if (categoryId == null) {
                // log.warn("DB에서 찾을 수 없는 카테고리 이름: {}", categoryName);
                continue;
            }

            List<String> names = entry.getValue();
            for (String name : names) {
                result.add(new ProductNameData(categoryId, name));
            }
        }

        return result;
    }

    // 배치 저장 로직 분리 (중복 코드 제거)
    private void saveBatch(List<Object[]> batchData) {
        jdbcTemplate.batchUpdate(
                """
                          INSERT INTO product_post
                          (id, user_id, category_id, title, name, price, description,
                        status, trade_status, view_count, liked_count, delete_status,
                        created_at, updated_at)
                          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                          """,
                batchData);
        batchData.clear();
    }

    /**
     * 이미지 데이터 생성
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
    }

    /**
     * 상품-이미지 관계 생성
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
