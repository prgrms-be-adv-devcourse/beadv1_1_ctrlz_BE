package com.settlement.job.step;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import com.settlement.job.dto.SettlementModel;
import com.settlement.job.processor.SettlementFeeProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementFeeStepConfig {

    private static final int RETRY_LIMIT = 3;
    private static final int SKIP_LIMIT = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    private final SettlementFeeProcessor settlementFeeProcessor;

    // Step 2: 생성된 정산 데이터(PENDING) 조회하여 수수료 계산 및 업데이트 (COMPLETED)
    @Bean
    public Step settlementFeeStep() {
        log.info("SettlementFeeStep 설정: 재시도 {}회, 스킵 {}회", RETRY_LIMIT, SKIP_LIMIT);
        return new StepBuilder("settlementFeeStep", jobRepository)
                .<SettlementModel, SettlementModel>chunk(1000, transactionManager)
                .reader(settlementFeeReader())
                .processor(settlementFeeProcessor)
                .writer(settlementUpdateWriter())
                // 재시도 설정: 일시적 DB 오류 시 재시도
                .faultTolerant()
                .retryLimit(RETRY_LIMIT)
                .retry(TransientDataAccessException.class)
                // 스킵 설정: 재시도 후에도 실패 시 건너뛰기
                .skipLimit(SKIP_LIMIT)
                .skip(Exception.class)
                .build();
    }

    @Bean
    public JdbcPagingItemReader<SettlementModel> settlementFeeReader() {
        return new JdbcPagingItemReaderBuilder<SettlementModel>()
                .name("settlementFeeReader")
                .dataSource(dataSource)
                .pageSize(1000)
                .fetchSize(1000)
                .rowMapper(new BeanPropertyRowMapper<>(SettlementModel.class))
                .queryProvider(createPendingSettlementQueryProvider())
                .build();
    }

    private PagingQueryProvider createPendingSettlementQueryProvider() {
        SqlPagingQueryProviderFactoryBean factory = new SqlPagingQueryProviderFactoryBean();
        factory.setDataSource(dataSource);
        factory.setSelectClause(
                "select id, order_item_id, user_id, amount, fee, net_amount, pay_type, status, settled_at, created_at, updated_at");
        factory.setFromClause("from settlements");
        factory.setWhereClause("where status = 'PENDING'");

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", Order.ASCENDING);
        factory.setSortKeys(sortKeys);

        try {
            return factory.getObject();
        } catch (Exception e) {
            throw new RuntimeException("쿼리 프로바이더 생성 실패", e);
        }
    }

    @Bean
    public JdbcBatchItemWriter<SettlementModel> settlementUpdateWriter() {
        return new JdbcBatchItemWriterBuilder<SettlementModel>()
                .dataSource(dataSource)
                .sql("UPDATE settlements SET fee = :fee, net_amount = :netAmount, status = :status, settled_at = :settledAt, updated_at = NOW() WHERE id = :id")
                .beanMapped()
                .build();
    }
}
