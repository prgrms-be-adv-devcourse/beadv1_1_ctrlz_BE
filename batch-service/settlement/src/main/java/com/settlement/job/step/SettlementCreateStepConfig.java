package com.settlement.job.step;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.settlement.job.dto.SettlementModel;
import com.settlement.job.dto.SettlementSourceDto;
import com.settlement.job.processor.SettlementCreateProcessor;
import com.settlement.job.reader.PaymentSettlementItemReader;

import lombok.RequiredArgsConstructor;
import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class SettlementCreateStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    private final PaymentSettlementItemReader paymentSettlementItemReader;
    private final SettlementCreateProcessor settlementCreateProcessor;

    // Step 1: 주문 서비스에서 데이터 가져와서 정산 데이터 생성 (PENDING, Fee/Amt = 0)
    @Bean
    public Step settlementCreateStep() {
        return new StepBuilder("settlementCreateStep", jobRepository)
                .<SettlementSourceDto, SettlementModel>chunk(1000, transactionManager)
                .reader(paymentSettlementItemReader)
                .processor(settlementCreateProcessor)
                .writer(settlementCreateWriter())
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<SettlementModel> settlementCreateWriter() {
        return new JdbcBatchItemWriterBuilder<SettlementModel>()
                .dataSource(dataSource)
                .sql("INSERT INTO settlements (id, order_item_id, user_id, amount, fee, net_amount, status, settled_at, created_at, updated_at) "
                        +
                        "VALUES (:id, :orderItemId, :userId, :amount, :fee, :netAmount, :status, :settledAt, :createdAt, :updatedAt)")
                .beanMapped()
                .build();
    }
}
