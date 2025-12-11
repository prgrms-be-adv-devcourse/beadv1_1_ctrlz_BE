package com.settlement.configuration;

import java.util.TimeZone;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.settlement.job.scheduler.SettlementBatchJob;

import lombok.extern.slf4j.Slf4j;

/**
 * Quartz 스케줄러 설정
 * 정산 배치를 매달 15일 새벽 3시(KST)에 자동 실행합니다.
 */
@Slf4j
@Configuration
public class SettlementScheduleConfig {

    /**
     * 정산 배치 JobDetail 설정
     */
    @Bean
    public JobDetail settlementJobDetail() {
        log.info("정산 배치 JobDetail 생성");
        return JobBuilder.newJob(SettlementBatchJob.class)
                .withIdentity("settlementBatchJob")
                .withDescription("정산 배치 Job - 매달 15일 새벽 3시 실행")
                .storeDurably()
                .build();
    }

    /**
     * 정산 배치 Trigger 설정
     * Cron: 0 0 3 15 * ? (매달 15일 새벽 3시)
     */
    @Bean
    public Trigger settlementTrigger(JobDetail settlementJobDetail) {
        log.info("정산 배치 Trigger 생성 - Cron: 0 0 3 15 * ? (매달 15일 새벽 3시, Asia/Seoul)");

        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
                .cronSchedule("0 0 3 15 * ?")
                .inTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        return TriggerBuilder.newTrigger()
                .forJob(settlementJobDetail)
                .withIdentity("settlementTrigger")
                .withDescription("매달 15일 새벽 3시에 정산 배치 실행")
                .withSchedule(scheduleBuilder)
                .build();
    }
}
