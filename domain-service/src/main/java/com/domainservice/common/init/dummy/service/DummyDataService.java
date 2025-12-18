package com.domainservice.common.init.dummy.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.domainservice.common.init.dummy.strategy.DummyGeneration;
import com.domainservice.common.init.dummy.strategy.MemoryGeneration;
import com.domainservice.common.init.dummy.strategy.TempTableGeneration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 더미 데이터 생성 메인 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DummyDataService {

	private final DummyDataGenerator dataGenerator;
	private final MemoryGeneration memoryStrategy;
	private final TempTableGeneration tempTableStrategy;

	public enum GenerationMode {
		MEMORY,           // 메모리 방식
		TEMP_TABLE,       // 임시 테이블 방식
	}

	private static final GenerationMode MODE = GenerationMode.TEMP_TABLE;

	private DummyGeneration getStrategy() {
		return switch (MODE) {
			case MEMORY -> memoryStrategy;
			case TEMP_TABLE -> tempTableStrategy;
		};
	}

	@Transactional
	public void generateAllDummyData(int productCount) {
		long startTime = System.currentTimeMillis();

		DummyGeneration strategy = getStrategy();
		log.info("=== 더미 데이터 생성 시작: {}개 ({}모드) ===", productCount, strategy.getType());

		// 카테고리와 태그 초기화
		dataGenerator.generateCategories();
		dataGenerator.generateTags();

		// 상품 생성
		strategy.generateProducts(productCount);

		// 이미지 및 태그 관계 생성
		dataGenerator.generateImagesAndRelations(productCount);
		dataGenerator.generateProductPostTags();

		long duration = System.currentTimeMillis() - startTime;

		log.info("=== 더미 데이터 생성 완료: {}초 (평균 {}건/초) ===",
			duration / 1000, duration > 0 ? productCount * 1000 / duration : 0);
	}

}