package com.domainservice.common.init.dummy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/dummy")
@RequiredArgsConstructor
public class DummyDataController {

	private final DummyDataGenerator generator;

	/**
	 * 더미 데이터 생성
	 *
	 * @param count 생성할 상품 수 (기본값: 100,000)
	 * @return 생성 결과
	 */
	@PostMapping("/generate")
	public ResponseEntity<Map<String, Object>>generateDummyData(
		@RequestParam(defaultValue = "100000")  int count) {

		log.info("더미 데이터 생성 요청: {}개", count);

		long startTime = System.currentTimeMillis();

		try {
			generator.generateAllDummyData(count);
			generator.printStatistics();

			long duration = System.currentTimeMillis() - startTime;

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("productCount", count);
			response.put("durationSeconds", duration / 1000);
			response.put("message", String.format("%d개의 상품 데이터를 %d초 만에 생성했습니다.",
				count, duration / 1000));

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("더미 데이터 생성 실패", e);

			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("error", e.getMessage());

			return ResponseEntity.internalServerError().body(response);
		}
	}

	/**
	 * 생성된 데이터 통계 조회
	 */
	@GetMapping("/statistics")
	public ResponseEntity<String> getStatistics() {
		generator.printStatistics();
		return ResponseEntity.ok("로그를 확인하세요.");
	}
}