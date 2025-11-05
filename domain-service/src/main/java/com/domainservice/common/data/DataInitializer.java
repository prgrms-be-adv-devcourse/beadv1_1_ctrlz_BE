package com.domainservice.common.data;

import com.domainservice.domain.product.brand.service.BrandService;
import com.domainservice.domain.product.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CategoryService categoryService;
    private final BrandService brandService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("========================================");
        log.info("데이터 초기화 시작");
        log.info("========================================");

        initializeCategories();
        initializeBrands();

        log.info("========================================");
        log.info("데이터 초기화 완료!");
        log.info("========================================");
    }

    /**
     * 카테고리 초기화
     */
    private void initializeCategories() {
        log.info("--- 카테고리 초기화 시작 ---");

        String[] categories = {
                "가구/인테리어",
                "가방/지갑",
                "가전제품",
                "기타",
                "도서",
                "생활용품",
                "스포츠/레저",
                "뷰티/미용",
                "신발",
                "식품",
                "유아동",
                "의류",
                "전자기기",
                "취미/게임",
                "반려동물용품"
        };


        for (String categoryName : categories) {
            categoryService.createIfNotExists(categoryName);
        }

        log.info("카테고리 {}개 초기화 완료", categories.length);
    }

    /**
     * 브랜드 초기화
     */
    private void initializeBrands() {
        log.info("--- 브랜드 초기화 시작 ---");

        String[] brands = {
                // 전자기기
                "애플", "삼성", "LG", "샤오미", "화웨이",

                // 의류/신발
                "나이키", "아디다스", "푸마", "리복", "뉴발란스",
                "유니클로", "자라", "H&M", "무신사", "탑텐",

                // 명품
                "구찌", "프라다", "샤넬", "루이비통", "에르메스",
                "버버리", "발렌시아가", "디올", "펜디", "셀린느",

                // 스포츠/아웃도어
                "노스페이스", "파타고니아", "콜롬비아", "K2", "블랙야크",

                // 가전
                "다이슨", "필립스", "브라운", "샤프", "파나소닉",

                // 화장품
                "에스티로더", "랑콤", "설화수", "라네즈", "이니스프리",

                // 기타
                "무인양품", "이케아", "한샘", "에이스침대", "템퍼",

                // 브랜드 없는 제품용
                "기타"
        };

        for (String brandName : brands) {
            brandService.createIfNotExists(brandName);
        }

        log.info("브랜드 {}개 초기화 완료", brands.length);
    }
}