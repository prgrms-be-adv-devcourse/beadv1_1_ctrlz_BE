package com.aiservice.infrastructure.qdrant;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.aiservice.domain.model.ProductVectorContent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(properties = {
                "custom.feign.url.domain-service=http://localhost:8081",
                "custom.feign.url.account-service=http://localhost:8082",
                "spring.data.redis.host=localhost",
                "spring.data.redis.port=6379",
                "spring.ai.openai.api-key=dummy"
})
class QdrantDataInsertionTest {

        @MockitoBean
        private ChatModel chatModel;

        @MockitoBean
        private EmbeddingModel embeddingModel;

        @Autowired
        private QdrantVectorRepository qdrantVectorRepository;

        @Test
        @DisplayName("더미 데이터 벡터 DB 삽입 테스트")
        void test1_insert_dummy_data() {
                // 더미 데이터 생성
                List<ProductVectorContent> dummyProducts = List.of(
                                ProductVectorContent.builder()
                                                .productId("prod_001")
                                                .title("아이폰 15 프로 맥스")
                                                .categoryName("디지털/가전") // Electronics/Smartphone -> 디지털/가전 (Assuming
                                                                        // mapping or just raw string)
                                                .tags(List.of("애플", "아이폰", "스마트폰", "ios"))
                                                .price(1900000)
                                                .description("티타늄 디자인과 A17 Pro 칩을 탑재한 역대 최고의 아이폰.")
                                                .status("SALE")
                                                .url("http://example.com/iphone15")
                                                .build(),

                                ProductVectorContent.builder()
                                                .productId("prod_002")
                                                .title("갤럭시 S24 울트라")
                                                .categoryName("디지털/가전")
                                                .tags(List.of("삼성", "갤럭시", "안드로이드", "ai"))
                                                .price(1690000)
                                                .description("갤럭시 AI가 온다. 삼성전자가 제공하는 최고의 성능.")
                                                .status("SALE")
                                                .url("http://example.com/s24ultra")
                                                .build(),

                                ProductVectorContent.builder()
                                                .productId("prod_003")
                                                .title("맥북 프로 16")
                                                .categoryName("디지털/가전")
                                                .tags(List.of("애플", "맥북", "노트북", "m3"))
                                                .price(4500000)
                                                .description("압도적인 성능. 눈길을 사로잡는 디자인. 역대 가장 진보한 맥북.")
                                                .status("SALE")
                                                .url("http://example.com/macbookpro")
                                                .build(),

                                ProductVectorContent.builder()
                                                .productId("prod_004")
                                                .title("소니 WH-1000XM5")
                                                .categoryName("디지털/가전")
                                                .tags(List.of("소니", "헤드폰", "노이즈캔슬링"))
                                                .price(450000)
                                                .description("업계 최고 수준의 노이즈 캔슬링으로 몰입감 넘치는 사운드.")
                                                .status("SALE")
                                                .url("http://example.com/sonyheadphones")
                                                .build(),

                                ProductVectorContent.builder()
                                                .productId("prod_005")
                                                .title("아이패드 에어 5세대")
                                                .categoryName("디지털/태블릿")
                                                .tags(List.of("애플", "아이패드", "태블릿", "M1"))
                                                .price(920000)
                                                .description("M1 칩의 강력한 성능. 얇고 가벼운 디자인의 아이패드 에어.")
                                                .status("SALE")
                                                .url("http://example.com/ipadair")
                                                .build(),

                                ProductVectorContent.builder()
                                                .productId("prod_006")
                                                .title("애플워치 시리즈 9")
                                                .categoryName("디지털/웨어러블")
                                                .tags(List.of("애플", "애플워치", "스마트워치", "헬스케어"))
                                                .price(599000)
                                                .description("더 밝아진 디스플레이와 강력한 칩셋. 건강을 위한 최고의 파트너.")
                                                .status("SALE")
                                                .url("http://example.com/applewatch")
                                                .build(),

                                ProductVectorContent.builder()
                                                .productId("prod_007")
                                                .title("갤럭시 탭 S9")
                                                .categoryName("디지털/태블릿")
                                                .tags(List.of("삼성", "갤럭시탭", "태블릿", "안드로이드"))
                                                .price(998000)
                                                .description("다이내믹 AMOLED 2X 디스플레이로 생생한 화질. 방수방진 지원.")
                                                .status("SALE")
                                                .url("http://example.com/galaxytab")
                                                .build(),

                                ProductVectorContent.builder()
                                                .productId("prod_008")
                                                .title("LG 그램 17")
                                                .categoryName("디지털/노트북")
                                                .tags(List.of("LG", "그램", "노트북", "가벼운노트북"))
                                                .price(2100000)
                                                .description("17인치 대화면에도 놀라운 가벼움. 어디서나 생산성을 높이세요.")
                                                .status("SALE")
                                                .url("http://example.com/lggram")
                                                .build(),

                                ProductVectorContent.builder()
                                                .productId("prod_009")
                                                .title("다이슨 V15 디텍트")
                                                .categoryName("가전/청소기")
                                                .tags(List.of("다이슨", "청소기", "무선청소기", "생활가전"))
                                                .price(1290000)
                                                .description("레이저가 보이지 않는 먼지까지 감지. 강력한 흡입력의 무선 청소기.")
                                                .status("SALE")
                                                .url("http://example.com/dysonv15")
                                                .build(),

                                ProductVectorContent.builder()
                                                .productId("prod_010")
                                                .title("닌텐도 스위치 OLED")
                                                .categoryName("디지털/게임")
                                                .tags(List.of("닌텐도", "게임기", "스위치", "휴대용게임기"))
                                                .price(415000)
                                                .description("선명한 OLED 디스플레이로 즐기는 다양한 닌텐도 게임.")
                                                .status("SALE")
                                                .url("http://example.com/nswitch")
                                                .build(),

                                ProductVectorContent.builder()
                                                .productId("prod_011")
                                                .title("플레이스테이션 5")
                                                .categoryName("디지털/게임")
                                                .tags(List.of("소니", "플스5", "게임기", "콘솔"))
                                                .price(688000)
                                                .description("초고속 SSD와 햅틱 피드백으로 경험하는 차세대 게이밍.")
                                                .status("SALE")
                                                .url("http://example.com/ps5")
                                                .build(),

                                ProductVectorContent.builder()
                                                .productId("prod_012")
                                                .title("고프로 히어로 12")
                                                .categoryName("디지털/카메라")
                                                .tags(List.of("고프로", "액션캠", "카메라", "여행"))
                                                .price(558000)
                                                .description("놀라운 화질과 흔들림 보정. 당신의 모험을 생생하게 기록하세요.")
                                                .status("SALE")
                                                .url("http://example.com/gopro12")
                                                .build(),

                                ProductVectorContent.builder()
                                                .productId("prod_013")
                                                .title("삼성 오디세이 G9")
                                                .categoryName("디지털/모니터")
                                                .tags(List.of("삼성", "모니터", "게이밍모니터", "울트라와이드"))
                                                .price(2200000)
                                                .description("49인치 커브드 OLED 화면이 선사하는 압도적인 몰입감.")
                                                .status("SALE")
                                                .url("http://example.com/odysseyg9")
                                                .build(),

                                ProductVectorContent.builder()
                                                .productId("prod_014")
                                                .title("로지텍 MX Master 3S")
                                                .categoryName("디지털/주변기기")
                                                .tags(List.of("로지텍", "마우스", "무선마우스", "사무용"))
                                                .price(139000)
                                                .description("초고속 스크롤과 편안한 그립감. 전문가를 위한 최고의 마우스.")
                                                .status("SALE")
                                                .url("http://example.com/mxmaster3s")
                                                .build());

                // 데이터 삽입
                for (ProductVectorContent product : dummyProducts) {
                        String docId = qdrantVectorRepository.addDocument(product);
                        log.info("벡터 DB 데이터 삽입 완료: {} (ID: {})", product.title(), docId);
                }
        }
}
