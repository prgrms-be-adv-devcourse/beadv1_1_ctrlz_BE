package com.domainservice.common.init.dummy.api;

import com.domainservice.common.init.dummy.service.DummyDataGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.domainservice.common.init.dummy.service.DummyDataService;
import com.domainservice.common.init.dummy.dto.DummyResultResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/admin/dummy")
@RequiredArgsConstructor
public class DummyDataController {

	private final DummyDataService generator;
	private final DummyDataGenerator esGenerator;

	@PostMapping("/generate")
	@ResponseStatus(HttpStatus.CREATED)
	public BaseResponse<DummyResultResponse> generateDummyData(
		@RequestParam(defaultValue = "100000") int count) {

		log.info("더미 데이터 생성 요청: {}개", count);
		long startTime = System.currentTimeMillis();

		generator.generateAllDummyData(count);

		long duration = System.currentTimeMillis() - startTime;

		DummyResultResponse result = DummyResultResponse.success(count, duration);

		return new BaseResponse<>(
			result,
			"%d개의 상품 데이터를 %d초 만에 생성했습니다."
				.formatted(result.productCount(), result.durationSeconds()));
	}

	@PostMapping("/generate/es")
	@ResponseStatus(HttpStatus.CREATED)
	public BaseResponse<DummyResultResponse> generateDummyDataEs(
			@RequestParam(defaultValue = "100000") int count) {

		log.info(" es 더미 데이터 생성 요청: {}개", count);
		long startTime = System.currentTimeMillis();

		esGenerator.syncProductsToElasticsearch(1000);

		long duration = System.currentTimeMillis() - startTime;

		DummyResultResponse result = DummyResultResponse.success(count, duration);

		return new BaseResponse<>(
				result,
				"ES %d개의 상품 데이터를 %d초 만에 생성했습니다."
						.formatted(result.productCount(), result.durationSeconds()));
	}

}
