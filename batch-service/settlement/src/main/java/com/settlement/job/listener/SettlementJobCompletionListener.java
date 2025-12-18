package com.settlement.job.listener;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Settlement 배치 작업 완료 후 시스템 종료를 처리하는 리스너
 */
@Slf4j
@Component
public class SettlementJobCompletionListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("=== Settlement 배치 작업 시작 ===");
        log.info("Job Name: {}", jobExecution.getJobInstance().getJobName());
        log.info("Job Parameters: {}", jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        BatchStatus status = jobExecution.getStatus();
        log.info("=== Settlement 배치 작업 완료 ===");
        log.info("Job Status: {}", status);
        log.info("Start Time: {}", jobExecution.getStartTime());
        log.info("End Time: {}", jobExecution.getEndTime());

        if (status == BatchStatus.COMPLETED) {
            log.info("Settlement 배치 작업이 성공적으로 완료되었습니다.");
        } else if (status == BatchStatus.FAILED) {
            log.error("Settlement 배치 작업이 실패했습니다.");
            jobExecution.getAllFailureExceptions().forEach(e -> 
                log.error("Error: {}", e.getMessage(), e));
        }

        log.info("애플리케이션을 종료합니다. (exit code: 0)");
        
        // 배치 작업 완료 후 시스템 종료
        System.exit(0);
    }
}
