package com.search.scheduler;

import java.time.LocalDateTime;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Quartz Job에서 Spring Batch Job 실행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchBatchJobLauncher extends QuartzJobBean {

    private final JobLauncher jobLauncher;
    private final Job searchHistoryJob;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("Quartz 스케줄러에 의해 배치 작업 시작: {}", LocalDateTime.now());
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("executedAt", LocalDateTime.now().toString())
                    .toJobParameters();

            jobLauncher.run(searchHistoryJob, jobParameters);
        } catch (Exception e) {
            log.error("배치 작업 실행 실패: {}", e.getMessage(), e);
            throw new JobExecutionException("배치 실행 실패", e);
        }
    }
}
