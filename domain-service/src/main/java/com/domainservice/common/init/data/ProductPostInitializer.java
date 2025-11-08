package com.domainservice.common.init.data;

import com.domainservice.domain.post.category.service.CategoryService;
import com.domainservice.domain.post.post.model.dto.request.CreateProductPostRequest;
import com.domainservice.domain.post.post.model.enums.ProductStatus;
import com.domainservice.domain.post.post.model.enums.TradeStatus;
import com.domainservice.domain.post.post.service.ProductPostService;
import com.domainservice.domain.post.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductPostInitializer {

    private final CategoryService categoryService;
    private final TagService tagService;
    private final ProductPostService productPostService;

    private final Random random = new Random();

    public void init() {
        log.info("--- 상품 게시글 초기화 시작 ---");

        List<String> categoryIds = categoryService.getAllCategoryIds();
        List<String> allTagIds = tagService.getAllTagIds();

        if (categoryIds.isEmpty()) {
            log.warn("카테고리가 없어 상품 게시글을 생성할 수 없습니다.");
            return;
        }

        if (allTagIds.isEmpty()) {
            log.warn("태그가 없습니다.");
        }

        // TODO: 실제 userId 필요
        String[] userIds = {"user-001", "user-002", "user-003", "user-004", "user-005"};
        ProductTemplate[] templates = createTemplates();

        int totalCount = 0;

        for (String categoryId : categoryIds) {
            int productsPerCategory = 5 + random.nextInt(6);

            for (int i = 0; i < productsPerCategory; i++) {
                try {
                    ProductTemplate template =
                            templates[random.nextInt(templates.length)];
                    String userId = userIds[random.nextInt(userIds.length)];

                    CreateProductPostRequest request = CreateProductPostRequest.builder()
                            .categoryId(categoryId)
                            .title(template.title())
                            .name(template.name())
                            .price(template.basePrice() + random.nextInt(50000))
                            .description(template.description())
                            .status(getRandomProductStatus())
                            .imageUrl("https://via.placeholder.com/400x300.png?text=" + template.name())
                            .tagIds(getRandomTagIds(allTagIds))
                            .build();

                    productPostService.createProductPost(request, userId);
                    totalCount++;

                } catch (Exception e) {
                    log.warn("상품 생성 실패: {}", e.getMessage());
                }
            }
        }

        log.info("상품 게시글 {}개 초기화 완료", totalCount);
    }

    private ProductStatus getRandomProductStatus() {
        int rand = random.nextInt(10);
        if (rand < 3) return ProductStatus.NEW;
        if (rand < 8) return ProductStatus.GOOD;
        return ProductStatus.FAIR;
    }

    private TradeStatus getRandomTradeStatus() {
        int rand = random.nextInt(10);
        if (rand < 7) return TradeStatus.SELLING;
        if (rand < 9) return TradeStatus.PROCESSING;
        return TradeStatus.SOLDOUT;
    }

    private List<String> getRandomTagIds(List<String> allTagIds) {
        if (allTagIds.isEmpty()) {
            return List.of();
        }

        int tagCount = 2 + random.nextInt(4);
        tagCount = Math.min(tagCount, allTagIds.size());

        List<String> selectedTagIds = new ArrayList<>();
        List<String> availableTagIds = new ArrayList<>(allTagIds);

        for (int i = 0; i < tagCount; i++) {
            int randomIndex = random.nextInt(availableTagIds.size());
            selectedTagIds.add(availableTagIds.remove(randomIndex));
        }

        return selectedTagIds;
    }

    public record ProductTemplate(
            String title,
            String name,
            int basePrice,
            String description
    ) {}

    public ProductTemplate[] createTemplates() {
        return new ProductTemplate[]{
                new ProductTemplate(
                        "아이폰 14 Pro 256GB 팝니다",
                        "아이폰 14 Pro 256GB",
                        950000,
                        "깨끗하게 사용한 아이폰입니다. 액정 파손 없고 배터리 효율 95%입니다."
                ),
                new ProductTemplate(
                        "맥북 프로 M2 판매합니다",
                        "맥북 프로 M2",
                        1800000,
                        "2023년 구매한 맥북입니다. 거의 사용하지 않아 판매합니다."
                ),
                new ProductTemplate(
                        "나이키 에어포스 1 (새상품)",
                        "나이키 에어포스 1",
                        89000,
                        "사이즈가 안맞아서 판매합니다. 한번도 신지 않았습니다."
                ),
                new ProductTemplate(
                        "무선 청소기 (다이슨 v11)",
                        "다이슨 무선청소기 v11",
                        350000,
                        "이사 가면서 정리합니다. 상태 좋습니다."
                ),
                new ProductTemplate(
                        "이케아 책상 팔아요",
                        "이케아 MICKE 책상",
                        45000,
                        "직거래만 가능합니다. 서울 강남 지역입니다."
                ),
                new ProductTemplate(
                        "Nintendo Switch OLED 화이트",
                        "닌텐도 스위치 OLED",
                        280000,
                        "동물의숲 에디션입니다. 게임 2개 포함 판매합니다."
                ),
                new ProductTemplate(
                        "에어팟 프로 2세대 급처",
                        "에어팟 프로 2세대",
                        200000,
                        "미개봉 새상품입니다. 선물받았는데 이미 있어서 판매합니다."
                ),
                new ProductTemplate(
                        "코딩책 모음 판매 (자바, 스프링)",
                        "개발서적 세트",
                        50000,
                        "토비의 스프링, 이펙티브 자바 등 총 5권입니다."
                ),
                new ProductTemplate(
                        "갤럭시 버즈2 프로 (새상품)",
                        "갤럭시 버즈2 프로",
                        150000,
                        "개봉만 하고 사용 안한 상품입니다."
                ),
                new ProductTemplate(
                        "캠핑 테이블 세트",
                        "캠핑용 폴딩 테이블",
                        60000,
                        "캠핑 한번 가고 사용 안해서 판매합니다."
                ),
                new ProductTemplate(
                        "아이패드 에어 5세대 64GB",
                        "아이패드 에어 5세대",
                        550000,
                        "애플펜슬 2세대 포함입니다. 케이스도 드립니다."
                ),
                new ProductTemplate(
                        "노스페이스 패딩 점퍼",
                        "노스페이스 눕시 패딩",
                        120000,
                        "작년 겨울에 산 거라 상태 좋습니다. 사이즈 100입니다."
                ),
                new ProductTemplate(
                        "로지텍 MX Master 3S 마우스",
                        "로지텍 MX Master 3S",
                        85000,
                        "사용감 거의 없습니다. 정품 박스 포함입니다."
                ),
                new ProductTemplate(
                        "강아지 사료 (로얄캐닌)",
                        "로얄캐닌 미니 어덜트",
                        35000,
                        "강아지가 안먹어서 판매합니다. 개봉 안한 새상품입니다."
                ),
                new ProductTemplate(
                        "LG 그램 노트북 17인치",
                        "LG 그램 17",
                        1200000,
                        "2023년형 그램입니다. i7, 16GB RAM, 512GB SSD"
                )
        };
    }
}
