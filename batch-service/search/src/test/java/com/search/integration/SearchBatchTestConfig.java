package com.search.integration;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.configuration.SearchBatchProperties;
import com.search.configuration.SearchBatchSkipListener;
import com.search.configuration.SearchLogLineMapper;
import com.search.configuration.SearchLogResourceProvider;
import com.search.dto.SearchHistoryDto;
import com.search.integration.fixture.TestLogFileGenerator;

@Configuration
@EnableAutoConfiguration
class SearchBatchTestConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public SearchBatchProperties searchBatchProperties() {
        return new SearchBatchProperties();
    }

    @Bean
    public SearchLogResourceProvider searchLogResourceProvider() {
        return new SearchLogResourceProvider();
    }

    @Bean
    public SearchLogLineMapper searchLogLineMapper(ObjectMapper objectMapper) {
        return new SearchLogLineMapper(objectMapper);
    }

    @Bean
    public SearchBatchSkipListener searchBatchSkipListener() {
        return new SearchBatchSkipListener();
    }

    @Bean
    public TestLogFileGenerator testLogFileGenerator() {
        return new TestLogFileGenerator();
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
            FlatFileItemReader<SearchHistoryDto> reader,
            JdbcBatchItemWriter<SearchHistoryDto> writer,
            SearchBatchSkipListener skipListener,
            SearchBatchProperties properties) {
        return new StepBuilder("searchHistoryStep", jobRepository)
                .<SearchHistoryDto, SearchHistoryDto>chunk(properties.getChunkSize(), transactionManager)
                .reader(reader)
                .writer(writer)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(properties.getSkipLimit())
                .listener(skipListener)
                .build();
    }

    @Bean
    @Primary
    public FlatFileItemReader<SearchHistoryDto> searchHistoryReader(
            SearchLogResourceProvider resourceProvider,
            SearchLogLineMapper lineMapper,
            SearchBatchProperties properties) {
        return new FlatFileItemReaderBuilder<SearchHistoryDto>()
                .name("searchHistoryReader")
                .resource(resourceProvider.createResource(properties.getLogPath()))
                .lineMapper(lineMapper)
                .build();
    }

    @Bean
    @Primary
    public JdbcBatchItemWriter<SearchHistoryDto> searchHistoryWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<SearchHistoryDto>()
                .dataSource(dataSource)
                .sql("INSERT INTO search_history (id, user_id, search_term, created_at) " +
                        "VALUES (:id, :userId, :searchTerm, :createdAt)")
                .beanMapped()
                .build();
    }

    @Bean
    public JdbcTemplate initSchema(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS search_history (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "user_id VARCHAR(255), " +
                "search_term VARCHAR(255), " +
                "created_at TIMESTAMP)");
        return jdbcTemplate;
    }
}
