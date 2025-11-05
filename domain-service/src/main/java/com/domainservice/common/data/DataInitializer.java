package com.domainservice.common.data;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import com.domainservice.domain.product.brand.service.BrandService;
import com.domainservice.domain.product.category.service.CategoryService;
import com.domainservice.domain.product.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CategoryService categoryService;
    private final BrandService brandService;
    private final TagService tagService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("========================================");
        log.info("데이터 초기화 시작");
        log.info("========================================");

        initializeCategories();
        initializeBrands();
        initializeTags();

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

    /**
     * 태그 초기화
     */
    private void initializeTags() {
        log.info("--- 태그 초기화 시작 ---");

        String[] tags = {
                // 상품 상태
                "새상품", "거의새것", "사용감있음", "중고", "리퍼",

                // 거래 방식
                "직거래", "택배거래", "반값택배", "끼택", "안전거래",
                "선입금", "착불", "반반택배", "편의점택배",

                // 가격 관련
                "가격제안가능", "네고가능", "가격흥정가능", "교환가능", "나눔",
                "급처", "급매", "반값", "최저가", "파격가",

                // 배송 관련
                "무료배송", "오늘출발", "당일발송", "빠른배송", "등기발송",

                // 제품 특징 - 전자기기
                "미개봉", "정품", "애플케어", "언락", "공기계",
                "128GB", "256GB", "512GB", "1TB",
                "블랙", "화이트", "블루", "퍼플", "골드",
                "아이폰", "갤럭시", "맥북", "아이패드", "에어팟",

                // 제품 특징 - 의류/잡화
                "프리사이즈", "XS", "S", "M", "L", "XL", "XXL",
                "새옷", "한번착용", "미착용", "택포함", "정품박스",
                "한정판", "콜라보", "빈티지", "리미티드",

                // 인기 브랜드 관련
                "나이키", "아디다스", "뉴발란스", "컨버스", "반스",
                "노스페이스", "파타고니아", "무신사", "구찌", "프라다",

                // 계절/시즌
                "겨울용", "여름용", "봄가을용", "사계절용", "방한",

                // 용도
                "출근룩", "데일리룩", "운동용", "학교용", "여행용",
                "홈트레이닝", "등산용", "캠핑용",

                // 기타 인기 키워드
                "인기상품", "베스트", "추천", "덤포함", "세트",
                "2개묶음", "3개세트", "대량구매", "벌크",
                "수입", "정식수입", "병행수입", "면세점",

                // 인증/검증
                "정품인증", "영수증포함", "보증서", "AS가능", "리셀가능"
        };

        for (String tagName : tags) {
            tagService.createIfNotExists(tagName);
        }

        log.info("태그 {}개 초기화 완료", tags.length);
    }
}