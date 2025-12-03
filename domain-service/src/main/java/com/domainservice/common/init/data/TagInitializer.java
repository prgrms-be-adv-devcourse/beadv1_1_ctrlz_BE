package com.domainservice.common.init.data;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.domainservice.common.init.dummy.DummyDataLoader;
import com.domainservice.domain.post.tag.service.TagService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class TagInitializer {

	private final TagService tagService;
	private final DummyDataLoader dataLoader;

	@Transactional
	public void init() {
		log.info("--- 태그 초기화 시작 ---");

		List<String> tags = dataLoader.loadLines("init/tags.txt");

		int createdCount = 0;

		for (String tagName : tags) {
			try {
				tagService.createIfNotExists(tagName);
				createdCount++;
			} catch (Exception e) {
				log.error("❌ 태그 생성 실패: {}", tagName, e);
			}
		}

		log.info("--- 태그 초기화 완료 ---");
		log.info("생성: {}개", createdCount);
	}
}