package com.domainservice.common.init.dummy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DummyDataGenerator {

	private final JdbcTemplate jdbcTemplate;

	/**
	 * 전체 더미 데이터 생성 프로세스
	 */
	@Transactional
	public void generateAllDummyData(int productCount) {
		long startTime = System.currentTimeMillis();

		log.info("=== 더미 데이터 생성 시작 ===");
		log.info("목표 상품 수: {}", productCount);

		// Step 1: 값 테이블 생성 확인
		log.info("Step 1: 값 테이블 확인 중...");
		checkValueTables();

		// Step 2: 상품 데이터 대량 생성 (CTE 활용)
		log.info("Step 2: 상품 데이터 생성 중...");
		generateProductPosts(productCount);

		// Step 3: 이미지 데이터 생성
		log.info("Step 3: 이미지 데이터 생성 중...");
		generateImages(productCount);

		// Step 4: 상품-이미지 관계 생성
		log.info("Step 4: 상품-이미지 관계 생성 중...");
		generateProductPostImages();

		// Step 5: 상품-태그 관계 생성
		log.info("Step 5: 상품-태그 관계 생성 중...");
		generateProductPostTags();

		long duration = System.currentTimeMillis() - startTime;
		log.info("=== 더미 데이터 생성 완료 ===");
		log.info("소요 시간: {}초", duration / 1000);
	}

	/**
	 * Step 1: 값 테이블 존재 확인
	 */
	private void checkValueTables() {
		try {
			// 임시 테이블이 없으면 생성
			jdbcTemplate.execute("""
                CREATE TEMPORARY TABLE IF NOT EXISTS title_prefixes (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    prefix VARCHAR(50)
                )
                """);

			Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM title_prefixes", Integer.class);

			if (count == null || count == 0) {
				log.info("값 테이블이 비어있습니다. 데이터를 생성합니다...");
				createValueTables();
			} else {
				log.info("값 테이블이 이미 존재합니다. ({}개 항목)", count);
			}
		} catch (Exception e) {
			log.info("값 테이블을 생성합니다...");
			createValueTables();
		}
	}

	/**
	 * 값 테이블 생성
	 */
	private void createValueTables() {
		// 제목 접두사
		jdbcTemplate.execute("""
            CREATE TEMPORARY TABLE IF NOT EXISTS title_prefixes (
                id INT AUTO_INCREMENT PRIMARY KEY,
                prefix VARCHAR(50)
            )
            """);

		jdbcTemplate.execute("""
            INSERT INTO title_prefixes (prefix) VALUES 
            ('판매합니다'), ('급매'), ('새상품'), ('중고'), ('미개봉'),
            ('택포'), ('직거래'), ('끌올'), ('가격인하'), ('네고가능'),
            ('정품'), ('한정판'), ('특가'), ('반값'), ('교환가능'),
            ('싸게팝니다'), ('팔아요'), ('나눔'), ('무료나눔'), ('저렴하게')
            """);

		// 상품명 테이블
		jdbcTemplate.execute("""
            CREATE TEMPORARY TABLE IF NOT EXISTS product_names (
                id INT AUTO_INCREMENT PRIMARY KEY,
                category_id VARCHAR(255),
                name VARCHAR(200)
            )
            """);

		jdbcTemplate.execute("""
            INSERT INTO product_names (category_id, name) VALUES 
            -- 가구/인테리어 (1번)
            ('01939a8c-1234-7000-8000-000000000001-category', '허먼밀러 의자'),
            ('01939a8c-1234-7000-8000-000000000001-category', '이케아 책상'),
            ('01939a8c-1234-7000-8000-000000000001-category', '시디즈 의자'),
            ('01939a8c-1234-7000-8000-000000000001-category', '침대 프레임'),
            ('01939a8c-1234-7000-8000-000000000001-category', '소파'),
            
            -- 가방/지갑 (2번)
            ('01939a8c-1234-7000-8000-000000000002-category', '루이비통 가방'),
            ('01939a8c-1234-7000-8000-000000000002-category', '구찌 지갑'),
            ('01939a8c-1234-7000-8000-000000000002-category', '샤넬 백팩'),
            
            -- 가전제품 (3번)
            ('01939a8c-1234-7000-8000-000000000003-category', '다이슨 청소기'),
            ('01939a8c-1234-7000-8000-000000000003-category', '삼성 냉장고'),
            ('01939a8c-1234-7000-8000-000000000003-category', 'LG 세탁기'),
            
            -- 의류 (4번)
            ('01939a8c-1234-7000-8000-000000000004-category', '노스페이스 패딩'),
            ('01939a8c-1234-7000-8000-000000000004-category', '나이키 후드티'),
            
            -- 신발 (5번)
            ('01939a8c-1234-7000-8000-000000000005-category', '나이키 에어맥스'),
            ('01939a8c-1234-7000-8000-000000000005-category', '아디다스 슈퍼스타'),
            
            -- 생활용품 (6번)
            ('01939a8c-1234-7000-8000-000000000006-category', '템퍼 베개'),
            ('01939a8c-1234-7000-8000-000000000006-category', '수건 세트'),
            
            -- 스포츠/레저 (7번)
            ('01939a8c-1234-7000-8000-000000000007-category', '요가매트'),
            ('01939a8c-1234-7000-8000-000000000007-category', '덤벨 세트'),
            
            -- 뷰티/미용 (8번)
            ('01939a8c-1234-7000-8000-000000000008-category', '다이슨 헤어드라이기'),
            ('01939a8c-1234-7000-8000-000000000008-category', '샤넬 향수'),
            
            -- 도서 (9번)
            ('01939a8c-1234-7000-8000-000000000009-category', '해리포터 전집'),
            ('01939a8c-1234-7000-8000-000000000009-category', '자기계발서'),
            
            -- 식품 (10번)
            ('01939a8c-1234-7000-8000-000000000010-category', '마이프로틴 5kg'),
            ('01939a8c-1234-7000-8000-000000000010-category', '올리브유'),
            
            -- 디지털/가전 (11번)
            ('01939a8c-1234-7000-8000-000000000011-category', '맥북 프로'),
            ('01939a8c-1234-7000-8000-000000000011-category', 'LG 그램'),
            ('01939a8c-1234-7000-8000-000000000011-category', '삼성 모니터'),
            
            -- 완구/취미 (12번)
            ('01939a8c-1234-7000-8000-000000000012-category', '레고 세트'),
            ('01939a8c-1234-7000-8000-000000000012-category', '닌텐도 스위치'),
            
            -- 휴대폰/태블릿 (13번)
            ('01939a8c-1234-7000-8000-000000000013-category', '아이폰 15 Pro'),
            ('01939a8c-1234-7000-8000-000000000013-category', '갤럭시 S24'),
            ('01939a8c-1234-7000-8000-000000000013-category', '아이패드'),
            
            -- 반려동물용품 (14번)
            ('01939a8c-1234-7000-8000-000000000014-category', '로얄캐닌 사료'),
            ('01939a8c-1234-7000-8000-000000000014-category', '강아지 옷'),
            
            -- 기타 (15번)
            ('01939a8c-1234-7000-8000-000000000015-category', '기타 용품')
            """);

		// 상태 키워드
		jdbcTemplate.execute("""
            CREATE TEMPORARY TABLE IF NOT EXISTS condition_words (
                id INT AUTO_INCREMENT PRIMARY KEY,
                word VARCHAR(50)
            )
            """);

		jdbcTemplate.execute("""
            INSERT INTO condition_words (word) VALUES 
            ('상태 최상'), ('거의 새것'), ('깨끗함'), ('흠집 없음'), ('박스 포함'),
            ('정품'), ('사용감 있음'), ('일부 사용'), ('케이스 포함'), ('보증서 있음'),
            ('미개봉'), ('새상품'), ('리퍼'), ('AS가능'), ('하자없음')
            """);

		// 설명 템플릿
		jdbcTemplate.execute("""
            CREATE TEMPORARY TABLE IF NOT EXISTS desc_templates (
                id INT AUTO_INCREMENT PRIMARY KEY,
                template TEXT
            )
            """);

		jdbcTemplate.execute("""
            INSERT INTO desc_templates (template) VALUES 
            ('몇 번 사용 안한 깨끗한 상품입니다.'),
            ('이사로 인해 급하게 판매합니다.'),
            ('선물 받았는데 사용하지 않아 판매합니다.'),
            ('업그레이드로 판매합니다. 상태 좋습니다.'),
            ('직거래 가능합니다. 택배비 별도입니다.'),
            ('사용감 거의 없습니다. 급매로 내놓습니다.'),
            ('정품 박스와 구성품 모두 있습니다.'),
            ('AS 기간이 남아있어요. 안심하고 구매하세요.'),
            ('흠집 하나 없이 깨끗합니다.'),
            ('가격 협상 가능합니다. 연락주세요!')
            """);

		log.info("값 테이블 생성 완료");
	}

	/**
	 * Step 2: 재귀 CTE를 사용한 대량 상품 데이터 생성
	 */
	@Transactional
	public void generateProductPosts(int count) {
		// 재귀 깊이 설정 (생성할 데이터 수 + 여유)
		jdbcTemplate.execute("SET SESSION cte_max_recursion_depth = " + (count + 100));

		String sql = """
            INSERT INTO product_post 
            (id, user_id, category_id, title, name, price, description, 
             status, trade_status, view_count, liked_count, delete_status, 
             created_at, updated_at)
            WITH RECURSIVE numbers AS (
                SELECT 1 AS n
                UNION ALL
                SELECT n + 1 FROM numbers WHERE n < ?
            ),
            random_users AS (
                SELECT 
                    n,
                    CONCAT(UUID(), '-user') AS user_id
                FROM numbers
            ),
            random_categories AS (
                SELECT 
                    ru.n,
                    ru.user_id,
                    (SELECT id FROM category ORDER BY RAND() LIMIT 1) AS category_id
                FROM random_users ru
            ),
            random_products AS (
                SELECT 
                    rc.n,
                    CONCAT(UUID(), '-product') AS product_id,
                    rc.user_id,
                    rc.category_id,
                    COALESCE(
                        (SELECT name FROM product_names pn WHERE pn.category_id = rc.category_id ORDER BY RAND() LIMIT 1),
                        '일반 상품'
                    ) AS product_name,
                    (SELECT prefix FROM title_prefixes ORDER BY RAND() LIMIT 1) AS title_prefix,
                    (SELECT word FROM condition_words ORDER BY RAND() LIMIT 1) AS condition_word,
                    (SELECT template FROM desc_templates ORDER BY RAND() LIMIT 1) AS description,
                    FLOOR(10000 + RAND() * 1990000) AS price,
                    FLOOR(RAND() * 10000) AS view_count,
                    FLOOR(RAND() * 500) AS liked_count,
                    ELT(FLOOR(1 + RAND() * 3), 'NEW', 'GOOD', 'FAIR') AS status,
                    ELT(FLOOR(1 + RAND() * 3), 'SELLING', 'PROCESSING', 'SOLDOUT') AS trade_status,
                    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY) AS created_date
                FROM random_categories rc
            )
            SELECT 
                product_id,
                user_id,
                category_id,
                CONCAT(title_prefix, ' ', product_name, ' ', condition_word),
                product_name,
                price,
                CONCAT(description, ' 가격: ', price, '원'),
                status,
                trade_status,
                view_count,
                liked_count,
                'N',
                created_date,
                created_date
            FROM random_products
            """;

		jdbcTemplate.update(sql, count);

		// 설정 초기화 (선택사항 - 기본값으로 복원)
		jdbcTemplate.execute("SET SESSION cte_max_recursion_depth = 1000");

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

	/**
	 * 생성된 데이터 통계 조회
	 */
	public void printStatistics() {
		Integer productCount = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM product_post", Integer.class);
		Integer imageCount = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM images WHERE image_target = 'PRODUCT'", Integer.class);
		Integer tagRelationCount = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM product_post_tag", Integer.class);

		log.info("=== 생성된 데이터 통계 ===");
		log.info("상품 수: {}", productCount);
		log.info("이미지 수: {}", imageCount);
		log.info("태그 관계 수: {}", tagRelationCount);

		List<String> categoryStats = jdbcTemplate.query(
			"""
			SELECT c.name, COUNT(p.id) as cnt
			FROM category c
			LEFT JOIN product_post p ON c.id = p.category_id
			GROUP BY c.id, c.name
			ORDER BY cnt DESC
			""",
			(rs, rowNum) -> String.format("%s: %d개", rs.getString("name"), rs.getInt("cnt"))
		);

		log.info("=== 카테고리별 분포 ===");
		categoryStats.forEach(log::info);
	}
}