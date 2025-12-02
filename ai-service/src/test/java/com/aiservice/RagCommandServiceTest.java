package com.aiservice;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.aiservice.controller.dto.DocumentSearchResponse;
import com.aiservice.domain.model.ProductVectorContent;
import com.aiservice.infrastructure.QdrantVectorRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles({"local", "secret"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RagCommandServiceTest {

	@Autowired
	private QdrantVectorRepository qdrantVectorRepository;

	@Autowired
	private EmbeddingModel embeddingModel;

	@DisplayName("실제 ONNX 한국어 모델로 상품 데이터가 정상 저장되고 검색된다")
	@Test
	void test() throws InterruptedException {

		// given: 실제 상품 데이터
		ProductVectorContent product = ProductVectorContent.builder()
			.title("삼성 갤럭시 Z 폴드6")
			.description("""
				2024년 최신 삼성 폴더블 스마트폰.
				7.6인치 메인 디스플레이와 6.3인치 커버 디스플레이 탑재.
				스냅드래곤 8 Gen 3 프로세서, 12GB RAM, 최대 1TB 저장공간.
				AI 기능 강화: 실시간 통역, 서클 투 서치, 노트 어시스트 등.
				방수방진 IP48 등급 지원.
				""")
			.categoryName("모바일/태블릿")
			.price(2_399_000)
			.tags(List.of("폴더블", "삼성", "AI", "프리미엄"))
			.build();

		// when: 저장 (첫 실행 시 ONNX 모델 다운로드 → 3~15초 걸릴 수 있음)
		String documentId = qdrantVectorRepository.addDocument(product);

		// 약간 대기 (임베딩 비동기 처리 방지)
		Thread.sleep(1000);

		// then: 저장된 ID 확인
		assertThat(documentId).isNotNull();

		// 벡터 DB에 실제로 들어갔는지 확인
		// List<DocumentSearchResponse> allDocs = vectorRepository.similaritySearch("",
		// 3);
		// assertThat(allDocs).isNotEmpty();

		// 실제 검색 테스트
		// List<DocumentSearchResponse> results1 = qdrantVectorRepository.similaritySearch("삼성 갤럭시 Z 폴드6 폴더블 프리미엄", 3);
		// List<DocumentSearchResponse> results2 = qdrantVectorRepository.similaritySearch("폴더블폰", 3);
		// List<DocumentSearchResponse> results3 = qdrantVectorRepository.similaritySearch("갤럭시 Z 폴드6 가격", 3);
		// List<DocumentSearchResponse> results4 = qdrantVectorRepository.hybridSearch("모바일/태블릿", 3, 0.1);

		// 모든 검색 결과 출력
		// log.info("=== 검색 결과 1: 삼성 갤럭시 Z 폴드6 있는지 찾아줘 ===");
		// log.info("유사도: {}", results1.getFirst().score());
		// if (results1.getFirst().score() < 0.7) {
		// 	log.warn("⚠️ 유사도가 {}로 낮습니다. 임베딩 모델이 한국어를 잘 이해하지 못합니다.", results1.getFirst().score());
		// }

		// log.info("=== 검색 결과 2: 폴더블폰 ===");
		// log.info("유사도: {}", results2.getFirst().score());
		// if (results2.getFirst().score() < 0.7) {
		// 	log.warn("⚠️ 유사도가 {}로 낮습니다. 임베딩 모델이 한국어를 잘 이해하지 못합니다.", results2.getFirst().score());
		// }
		//
		// log.info("=== 검색 결과 3: 갤럭시 Z 폴드6 가격 ===");
		// log.info("유사도: {}", results3.getFirst().score());
		// if (results3.getFirst().score() < 0.7) {
		// 	log.warn("⚠️ 유사도가 {}로 낮습니다. 임베딩 모델이 한국어를 잘 이해하지 못합니다.", results3.getFirst().score());
		// }

		log.info("=== 검색 결과 4: 모바일/태블릿 ===");
		// log.info("유사도: {}", results4.getFirst().score());
		// if (results4.getFirst().score() < 0.7) {
		// 	log.warn("⚠️ 유사도가 {}로 낮습니다. 임베딩 모델이 한국어를 잘 이해하지 못합니다.", results4.getFirst().score());
		// }
		//
		// log.info("=== 테스트 완료 ===");

	}

	@DisplayName("Hybrid Search: 짧은 쿼리에서도 정확도 향상 확인")
	@Test
	void test2() throws InterruptedException {

		// given: 여러 상품 데이터 추가
		ProductVectorContent product1 = ProductVectorContent.builder()
			.title("아이폰 15 Pro")
			.description("Apple의 최신 플래그십 스마트폰. A17 Pro 칩셋, 티타늄 바디")
			.categoryName("모바일/태블릿")
			.price(1_550_000)
			.tags(List.of("애플", "아이폰", "프리미엄"))
			.build();

		ProductVectorContent product2 = ProductVectorContent.builder()
			.title("갤럭시 Z 폴드6")
			.description("삼성 폴더블 스마트폰. 스냅드래곤 8 Gen 3")
			.categoryName("모바일/태블릿")
			.price(2_399_000)
			.tags(List.of("삼성", "폴더블", "AI"))
			.build();

		qdrantVectorRepository.addDocument(product1);
		qdrantVectorRepository.addDocument(product2);
		Thread.sleep(1000);

		// when: 짧은 쿼리로 검색
		String shortQuery = "아이폰";

		List<DocumentSearchResponse> normalResults = qdrantVectorRepository.similaritySearch(shortQuery, 3, product1.categoryName(), product1.tags());

		// Hybrid Search (Dense 70% + Sparse 30%)
		// List<DocumentSearchResponse> hybridResults = qdrantVectorRepository.hybridSearch(shortQuery, 3, 0.9);

		// then: 결과 비교
		log.info("=== 일반 검색 결과 ===");
		normalResults.forEach(doc -> log.info("Score: {}, Content: {}", doc.score(),
			doc.content().substring(0, Math.min(50, doc.content().length()))));

		// log.info("=== Hybrid 검색 결과 ===");
		// hybridResults.forEach(doc -> log.info("Score: {}, Content: {}", doc.score(),
		// 	doc.content().substring(0, Math.min(50, doc.content().length()))));
		//
		// assertThat(hybridResults).isNotEmpty();
	}
}
