package com.settlement.job.step;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

import com.settlement.job.dto.SettlementModel;
import com.settlement.job.dto.SettlementSourceDto;
import com.settlement.job.processor.SettlementCreateProcessor;
import com.settlement.job.reader.PaymentSettlementItemReader;

import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementCreateStepConfig {

    private static final int RETRY_LIMIT = 3;
    private static final int SKIP_LIMIT = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    private final PaymentSettlementItemReader paymentSettlementItemReader;
    private final SettlementCreateProcessor settlementCreateProcessor;

    // Step 1: 주문 서비스에서 데이터 가져와서 정산 데이터 생성 (PENDING, Fee/Amt = 0)
    @Bean
    public Step settlementCreateStep() {
        log.info("SettlementCreateStep 설정: 재시도 {}회, 스킵 {}회", RETRY_LIMIT, SKIP_LIMIT);
        return new StepBuilder("settlementCreateStep", jobRepository)
                .<SettlementSourceDto, SettlementModel>chunk(1000, transactionManager)
                .reader(paymentSettlementItemReader)
                .processor(settlementCreateProcessor)
                .writer(settlementCreateWriter())
                // 재시도 설정: 네트워크 오류 및 일시적 DB 오류 시 재시도
                .faultTolerant()
                .retryLimit(RETRY_LIMIT)
                .retry(FeignException.class)
                .retry(RetryableException.class)
                .retry(ConnectException.class)
                .retry(SocketTimeoutException.class)
                .retry(TransientDataAccessException.class)
                // 스킵 설정: 재시도 후에도 실패 시 건너뛰기
                .skipLimit(SKIP_LIMIT)
                .skip(FeignException.class)
                .skip(RetryableException.class)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<SettlementModel> settlementCreateWriter() {
        return new JdbcBatchItemWriterBuilder<SettlementModel>()
                .dataSource(dataSource)
                .sql("INSERT INTO settlements (id, order_item_id, user_id, amount, fee, net_amount, pay_type, status, settled_at, created_at, updated_at) "
                        +
                        "VALUES (:id, :orderItemId, :userId, :amount, :fee, :netAmount, :payType, :status, :settledAt, :createdAt, :updatedAt)")
                .beanMapped()
                .build();
    }
}
