package com.domainservice.common.init.data;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.domainservice.domain.post.tag.service.TagService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class TagInitializer {

	private final TagService tagService;

	public void init() {
		log.info("--- 태그 초기화 시작 ---");

		String[] tags = {
			"새상품", "거의새것", "중고", "미개봉", "상태최상", "직거래",
			"택배거래", "안전거래", "무료배송", "택포", "급매",
			"네고가능", "가격제안환영", "할인가능", "파격세일",
			"한정판", "명품", "정품", "선물용", "인기상품"
		};

		for (String tagName : tags) {
			tagService.createIfNotExists(tagName);
		}

		log.info("태그 {}개 초기화 완료", tags.length);
	}
}