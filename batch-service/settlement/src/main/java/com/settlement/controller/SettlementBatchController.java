package com.settlement.controller;

import java.time.LocalDateTime;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;

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
            @RequestParam String startDate,
            @RequestParam String endDate) {

        try {
            LocalDateTime startDateTime;
            LocalDateTime endDateTime;

            startDateTime = LocalDateTime.parse(startDate);
            endDateTime = LocalDateTime.parse(endDate);

            log.info("정산 배치 수동 실행 요청 - 정산 기간: {} ~ {}", startDateTime, endDateTime);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("startDate", startDateTime.toString())
                    .addString("endDate", endDateTime.toString())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            // 배치 실행
            jobLauncher.run(settlementJob, jobParameters);
            String message = "정산 배치 실행 완료 - 정산 기간: %s ~ %s".formatted(startDateTime, endDateTime);
            log.info(message);

            return ResponseEntity.ok(message);

        } catch (Exception e) {
            log.error("정산 배치 수동 실행 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body("정산 배치 실행 실패: " + e.getMessage());
        }
    }
}
