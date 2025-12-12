package com.search.scheduler;

import java.util.TimeZone;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz 스케줄링 설정
 * 매일 새벽 2시에 배치 실행
 */
@Configuration
public class SearchBatchScheduleConfiguration {

    @Bean
    public JobDetail searchBatchJobDetail() {
        return JobBuilder.newJob(SearchBatchJobLauncher.class)
                .withIdentity("searchBatchJob", "batchGroup")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger searchBatchTrigger(JobDetail searchBatchJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(searchBatchJobDetail)
                .withIdentity("searchBatchTrigger", "batchGroup")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 2 * * ?")
                        .inTimeZone(TimeZone.getTimeZone("Asia/Seoul")))
                .build();
    }
}
