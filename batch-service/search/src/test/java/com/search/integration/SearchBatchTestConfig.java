package com.search.integration;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.configuration.SearchLogLineMapper;
import com.search.configuration.SearchLogResourceProvider;
import com.search.dto.UserBehaviorDto;
import com.search.listener.SearchBatchSkipListener;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 통합 테스트용 배치 설정 - 순차 처리 방식
 * 테스트에서는 순차 처리를 사용 (MultiResourceItemReader)
 * 운영 환경에서는 SearchBatchConfig의 파티셔닝 사용
 */
@Slf4j
@Configuration
@EnableAutoConfiguration
@Getter
@Setter
class SearchBatchTestConfig {

    // 테스트에서 setter를 통해 동적으로 설정됨
    // @TempDir 경로로 설정되므로 @Value 사용하지 않음
    private String logDirectory = System.getProperty("java.io.tmpdir");
    private String logPattern = "*.log.gz";

    private static final int CHUNK_SIZE = 1000;
    private static final int SKIP_LIMIT = 100;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public SearchLogResourceProvider searchLogResourceProvider() {
        return new SearchLogResourceProvider();
    }

    @Bean
    public SearchBatchSkipListener searchBatchSkipListener() {
        return new SearchBatchSkipListener();
    }

    @Bean
    @Primary
    public Job searchHistoryJob(JobRepository jobRepository, Step searchHistoryStep) {
        return new JobBuilder("searchHistoryJob", jobRepository)
                .start(searchHistoryStep)
                .build();
    }

    @Bean
    @Primary
    public Step searchHistoryStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            MultiResourceItemReader<UserBehaviorDto> reader,
            JdbcBatchItemWriter<UserBehaviorDto> writer,
            SearchBatchSkipListener skipListener) {
        return new StepBuilder("searchHistoryStep", jobRepository)
                .<UserBehaviorDto, UserBehaviorDto>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .writer(writer)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(SKIP_LIMIT)
                .listener(skipListener)
                .build();
    }

    /**
     * 테스트용 순차 처리 Reader
     * 동적으로 설정된 디렉토리/패턴을 사용
     * 
     * 주의: @StepScope Bean은 Step 실행 시점에 생성되지만,
     * 테스트에서 setLogDirectory/setLogPattern으로 변경된 값을 사용하려면
     * getter를 통해 참조해야 합니다.
     */
    @Bean
    @Primary
    @StepScope
    public MultiResourceItemReader<UserBehaviorDto> searchHistoryReader(
            SearchLogResourceProvider resourceProvider) {

        // 현재 시점의 logDirectory/logPattern 값 사용 (setter로 변경된 값 반영)
        String currentLogDirectory = this.logDirectory;
        String currentLogPattern = this.logPattern;

        log.info("테스트 Reader 초기화 - 디렉토리: {}, 패턴: {}", currentLogDirectory, currentLogPattern);

        Resource[] resources = resourceProvider.createResources(currentLogDirectory, currentLogPattern);

        MultiResourceItemReader<UserBehaviorDto> multiReader = new MultiResourceItemReader<>();
        multiReader.setResources(resources);
        multiReader.setDelegate(singleFileReader());

        log.info("테스트 Reader 초기화 완료 - 파일 수: {}", resources.length);
        return multiReader;
    }

    private FlatFileItemReader<UserBehaviorDto> singleFileReader() {
        FlatFileItemReader<UserBehaviorDto> reader = new FlatFileItemReader<UserBehaviorDto>() {
            @Override
            public void setResource(@org.springframework.lang.NonNull Resource resource) {
                super.setResource(resource);
                String filename = resource.getFilename();
                String behaviorType = "SEARCH";
                if (filename != null && filename.contains("item-view")) {
                    behaviorType = "VIEW";
                }
                this.setLineMapper(new SearchLogLineMapper(behaviorType));
            }
        };
        reader.setName("searchHistorySingleFileReader");
        return reader;
    }

    @Bean
    @Primary
    public JdbcBatchItemWriter<UserBehaviorDto> searchHistoryWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<UserBehaviorDto>()
                .dataSource(dataSource)
                .sql("INSERT INTO user_behavior (id, user_id, behavior_value, behavior_type, created_at) " +
                        "VALUES (:id, :userId, :behaviorValue, :behaviorType, :createdAt)")
                .beanMapped()
                .build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
