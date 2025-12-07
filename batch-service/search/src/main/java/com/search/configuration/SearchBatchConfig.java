package com.search.configuration;

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
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.search.dto.SearchHistoryDto;
import com.search.listener.SearchBatchSkipListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 검색 이력 배치 작업 설정
 * 파티셔닝을 통한 다중 로그 파일 병렬 처리 지원
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SearchBatchConfig {

        private final JobRepository jobRepository;
        private final PlatformTransactionManager transactionManager;
        private final DataSource dataSource;

        private final SearchLogResourceProvider resourceProvider;
        private final SearchLogLineMapper lineMapper;
        private final SearchBatchSkipListener skipListener;
        private final SearchFilePartitioner partitioner;

        @Value("${batch.search.log-directory:logs}")
        private String logDirectory;

        @Value("${batch.search.log-pattern:item-view.log*.gz}")
        private String logPattern;

        private static final int CHUNK_SIZE = 1000;
        private static final int SKIP_LIMIT = 100;
        private static final int POOL_SIZE = 4;

        private static final String UPSERT_SQL = "INSERT INTO search_history (id, user_id, search_term, created_at) " +
                        "VALUES (:id, :userId, :searchTerm, :createdAt) " +
                        "ON DUPLICATE KEY UPDATE " + // MySQL upsert 구문
                        "user_id = VALUES(user_id), " +
                        "search_term = VALUES(search_term), " +
                        "created_at = VALUES(created_at)";

        @Bean
        public Job searchHistoryJob() {
                log.info("검색 이력 배치 작업 초기화 (파티셔닝 모드) - chunkSize: {}, skipLimit: {}, poolSize: {}, directory: {}, pattern: {}",
                                CHUNK_SIZE, SKIP_LIMIT, POOL_SIZE, logDirectory, logPattern);

                return new JobBuilder("searchHistoryJob", jobRepository)
                                .start(masterStep())
                                .build();
        }

        /**
         * 마스터 Step
         */
        @Bean
        public Step masterStep() {
                return new StepBuilder("masterStep", jobRepository)
                                .partitioner("slaveStep", partitioner)
                                .step(slaveStep())
                                .taskExecutor(batchTaskExecutor())
                                .build();
        }

        /**
         * 슬레이브 Step
         */
        @Bean
        public Step slaveStep() {
                return new StepBuilder("slaveStep", jobRepository)
                                .<SearchHistoryDto, SearchHistoryDto>chunk(CHUNK_SIZE, transactionManager)
                                .reader(partitionedReader(null))
                                .writer(searchHistoryWriter())
                                .faultTolerant()
                                .skip(Exception.class)
                                .skipLimit(SKIP_LIMIT)
                                .listener(skipListener)
                                .build();
        }

        /**
         * 배치 작업용 스레드풀 설정
         */
        @Bean
        public TaskExecutor batchTaskExecutor() {
                ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
                executor.setCorePoolSize(POOL_SIZE);
                executor.setMaxPoolSize(POOL_SIZE * 2);
                executor.setQueueCapacity(POOL_SIZE * 4);
                executor.setThreadNamePrefix("batch-partition-");
                executor.setWaitForTasksToCompleteOnShutdown(true);
                executor.initialize();

                log.info("배치 스레드풀 초기화 - corePoolSize: {}, maxPoolSize: {}", POOL_SIZE, POOL_SIZE * 2);
                return executor;
        }

        /**
         * 파티션별 파일을 처리하는 Reader
         * 
         * @param filePath StepExecutionContext에서 주입받는 파일 경로
         */
        @Bean
        @StepScope
        public FlatFileItemReader<SearchHistoryDto> partitionedReader(
                        @Value("#{stepExecutionContext['filePath']}") String filePath) {

                if (filePath == null) {
                        log.warn("파일 경로가 null입니다. Bean 초기화 중일 수 있습니다.");
                        return new FlatFileItemReaderBuilder<SearchHistoryDto>()
                                        .name("partitionedReader")
                                        .lineMapper(lineMapper)
                                        .build();
                }

                Resource resource = resourceProvider.createResource(filePath);
                log.info("파티션 Reader 초기화 - 파일: {}", filePath);

                return new FlatFileItemReaderBuilder<SearchHistoryDto>()
                                .name("partitionedReader")
                                .resource(resource)
                                .lineMapper(lineMapper)
                                .build();
        }

        @Bean
        public JdbcBatchItemWriter<SearchHistoryDto> searchHistoryWriter() {
                return new JdbcBatchItemWriterBuilder<SearchHistoryDto>()
                                .dataSource(dataSource)
                                .sql(UPSERT_SQL)
                                .beanMapped()
                                .build();
        }
}
