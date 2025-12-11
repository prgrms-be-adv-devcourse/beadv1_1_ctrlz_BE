package com.settlement.controller;

import java.time.LocalDateTime;
import java.time.YearMonth;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 정산 배치 수동 실행 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/batch/settlement")
@RequiredArgsConstructor
public class SettlementBatchController {

    private final JobLauncher jobLauncher;

    @Qualifier("settlementJob")
    private final Job settlementJob;

    /**
     * 정산 배치 수동 실행
     * 
     * @param startDate 정산 시작일 (없으면 전월 1일)
     * @param endDate   정산 종료일 (없으면 전월 마지막날)
     * @return 실행 결과
     */
    @PostMapping("/manual")
    public ResponseEntity<String> runSettlementBatch(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        try {
            // 파라미터가 없으면 전월 1일 ~ 마지막날로 설정
            if (startDate == null || endDate == null) {
                YearMonth lastMonth = YearMonth.from(LocalDateTime.now().minusMonths(1));
                startDate = lastMonth.atDay(1).atStartOfDay();
                endDate = lastMonth.atEndOfMonth().atTime(23, 59, 59);
            }

            log.info("정산 배치 수동 실행 요청 - 정산 기간: {} ~ {}", startDate, endDate);

            // JobParameters 생성
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("startDate", startDate.toString())
                    .addString("endDate", endDate.toString())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            // 배치 실행
            jobLauncher.run(settlementJob, jobParameters);

            String message = String.format("정산 배치 실행 완료 - 정산 기간: %s ~ %s", startDate, endDate);
            log.info(message);

            return ResponseEntity.ok(message);

        } catch (Exception e) {
            log.error("정산 배치 수동 실행 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body("정산 배치 실행 실패: " + e.getMessage());
        }
    }
}
