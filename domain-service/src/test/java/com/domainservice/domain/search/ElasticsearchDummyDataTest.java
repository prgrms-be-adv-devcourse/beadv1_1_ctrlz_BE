package com.domainservice.domain.search;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.common.model.persistence.BaseEntity;
import com.common.model.vo.ProductStatus;
import com.common.model.vo.TradeStatus;
import com.domainservice.domain.search.model.entity.dto.document.ProductPostDocumentEntity;
import com.domainservice.domain.search.repository.ProductPostElasticRepository;

@Disabled
@DisplayName("es 데이터 삽입")
@SpringBootTest
@ActiveProfiles({"local", "secret"})
public class ElasticsearchDummyDataTest {

	@Autowired
	private ProductPostElasticRepository productPostElasticRepository;

	private final List<ProductPostDocumentEntity> insertedData = new ArrayList<>();

	@BeforeEach
	void setUpEach(){
		productPostElasticRepository.deleteAll();
	}

	@Test
	@DisplayName("엘라스틱서치 더미 데이터 10개 삽입 및 카테고리별 상품명 설정")
	void test1() {

		List<String> categories = new ArrayList<>(List.of(
			"수입명품",
			"패션의류",
			"패션잡화",
			"뷰티",
			"출산/유아동",
			"모바일/태블릿", // <-- 대상 카테고리
			"가전제품",
			"노트북/PC",
			"카메라/캠코더",
			"가구/인테리어",
			"리빙/생활",
			"게임",
			"반려동물/취미",
			"도서/음반/문구",
			"티켓/쿠폰",
			"스포츠",
			"레저/여행"
		));

		// 다양한 모바일/태블릿 기종 목록
		final String[] MOBILE_AND_TABLET_NAMES = {
			"아이폰 15 Pro Max",
			"아이폰 15 Pro",
			"아이폰 15",
			"아이폰 SE 3세대",
			"갤럭시 S24 Ultra",
			"갤럭시 S24",
			"갤럭시 Z 폴드 5",
			"갤럭시 Z 플립 5",
			"아이패드 Pro M2",
			"아이패드 Air 5세대",
			"갤럭시 탭 S9 Ultra",
			"샤오미 14 Pro",
			"구글 픽셀 8 Pro",
			"Surface Pro 9"
		};


		IntStream.rangeClosed(1, 1000)
			.parallel()
			.forEach(i -> {
				// 1. 카테고리 목록 복사 및 섞기
				List<String> localCategories = new ArrayList<>(categories);
				Collections.shuffle(localCategories);

				// 2. 랜덤 카테고리 선택
				String selectedCategory = localCategories.getFirst();

				// 3. 상품명 초기화 (기본값)
				String productTitle = "테스트 상품 " + i;

				// 4. 조건에 따른 상품명 변경 (핵심 로직)
				if ("모바일/태블릿".equals(selectedCategory)) {
					// i % 배열 길이로 순환하며 다양한 모바일 기종 선택
					String randomMobileName = MOBILE_AND_TABLET_NAMES[i % MOBILE_AND_TABLET_NAMES.length];

					// 상품명 업데이트
					productTitle = randomMobileName + " (더미) " + i;
				}

				ProductPostDocumentEntity entity = ProductPostDocumentEntity.builder()
					.id(UUID.randomUUID().toString())
					.userId("testUser" + i)
					.name(productTitle)
					.title("테스트 제목 " + i)
					.description("테스트 설명입니다. HTML 태그 없이 텍스트만 들어갑니다. " + i)
					.tags(Arrays.asList("테스트", "더미", "데이터" + i))
					.categoryName(selectedCategory)
					.price(10000L * i)
					.likedCount((long) i)
					.viewCount((long) (i * 10))
					.status(ProductStatus.NEW.name())
					.tradeStatus(TradeStatus.SELLING.name())
					.deleteStatus(BaseEntity.DeleteStatus.N.name())
					.createdAt(LocalDateTime.now())
					.updatedAt(LocalDateTime.now())
					.primaryImageUrl("https://example.com/image.jpg")
					.build();


				insertedData.add(entity);
			});

		System.out.println("생성된 데이터 수: " + insertedData.size());

		productPostElasticRepository.saveAll(insertedData);

		System.out.println("엘라스틱서치 더미 데이터 삽입 완료" + insertedData.size());
	}
}
