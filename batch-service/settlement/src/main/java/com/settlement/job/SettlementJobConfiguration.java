package com.settlement.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.settlement.job.listener.SettlementJobCompletionListener;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SettlementJobConfiguration {

    private final JobRepository jobRepository;
    private final SettlementJobCompletionListener settlementJobCompletionListener;


    @Qualifier("settlementJob")
    @Bean
    public Job settlementJob(@Qualifier("settlementCreateStep") Step settlementCreateStep,
            @Qualifier("settlementFeeStep") Step settlementFeeStep) {
        return new JobBuilder("settlementJob", jobRepository)
                .listener(settlementJobCompletionListener)
                .start(settlementCreateStep)
                .next(settlementFeeStep)
                .build();
    }
}
