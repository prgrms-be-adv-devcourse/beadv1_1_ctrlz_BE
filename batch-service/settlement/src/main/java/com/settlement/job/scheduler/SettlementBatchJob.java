package com.settlement.job.scheduler;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.quartz.JobExecutionContext;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Quartz 스케줄러를 통해 정산 배치를 실행하는 Job 클래스
 * 매달 15일 새벽 3시에 실행되어 전월 데이터를 정산 처리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementBatchJob extends QuartzJobBean {

    private final JobLauncher jobLauncher;

    @Qualifier("settlementJob")
    private final Job settlementJob;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        try {
            log.info("정산 배치 스케줄링 시작");

            // 전월 1일 ~ 마지막날 계산
            LocalDateTime now = LocalDateTime.parse(LocalDateTime.now().toString(), DateTimeFormatter.ISO_DATE_TIME);
            YearMonth lastMonth = YearMonth.from(now.minusMonths(1));
            LocalDateTime startDate = lastMonth.atDay(1).atStartOfDay();
            LocalDateTime endDate = lastMonth.atEndOfMonth().atTime(23, 59, 59);

            log.info("정산 기간: {} ~ {}", startDate, endDate);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("startDate", startDate.toString())
                    .addString("endDate", endDate.toString())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            // 배치 실행
            jobLauncher.run(settlementJob, jobParameters);

            log.info("정산 배치 스케줄링 완료");
        } catch (Exception e) {
            log.error("정산 배치 스케줄링 실행 중 오류 발생", e);
            throw new RuntimeException("정산 배치 실행 실패", e);
        }
    }
}
