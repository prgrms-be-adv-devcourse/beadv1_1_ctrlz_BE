package com.domainservice.common.init.data;

import org.springframework.stereotype.Component;

import com.domainservice.domain.post.category.service.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
// @Profile({"local", "dev"})
@RequiredArgsConstructor
public class CategoryInitializer {

    private final CategoryService categoryService;

    public void init() {
        log.info("--- 카테고리 초기화 시작 ---");

        String[] categories = {
                "가구/인테리어", "가방/지갑", "가전제품"
                , "기타", "도서",
                "생활용품", "스포츠/레저", "뷰티/미용", "신발", "식품",
                "유아동", "의류", "전자기기", "취미/게임", "반려동물용품"
        };

        for (String categoryName : categories) {
            categoryService.createIfNotExists(categoryName);
        }

        log.info("카테고리 {}개 초기화 완료", categories.length);
    }
}
