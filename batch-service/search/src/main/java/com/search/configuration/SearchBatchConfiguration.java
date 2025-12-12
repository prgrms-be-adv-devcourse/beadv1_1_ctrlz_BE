package com.search.configuration;

import com.search.dto.UserBehaviorDto;
import com.search.listener.SearchBatchSkipListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * 검색 이력 배치 작업 설정
 * 파티셔닝 사용
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SearchBatchConfiguration {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final DataSource dataSource;

	private final SearchLogResourceProvider resourceProvider;
	private final SearchBatchSkipListener skipListener;
	private final SearchFilePartitioner partitioner;

	@Value("${batch.search.log-directory:logs}")
	private String logDirectory;

	@Value("${batch.search.log-pattern:item-view.log*,search-view.log*}")
	private String logPattern;

	@Value("${batch.search.chunk-size:1000}")
	private int chunkSize;

	@Value("${batch.search.skip-limit:100}")
	private int skipLimit;

	@Value("${batch.search.pool-size:4}")
	private int poolSize;

	private static final String UPSERT_SQL =
		"INSERT INTO user_behavior (id, user_id, behavior_value, behavior_type, created_at) "
			+
			"VALUES (:id, :userId, :behaviorValue, :behaviorType, :createdAt) " +
			"ON DUPLICATE KEY UPDATE " +
			"user_id = :userId, " +
			"behavior_value = :behaviorValue, " +
			"behavior_type = :behaviorType, " +
			"created_at = :createdAt";

	@Qualifier("searchHistoryJob")
	@Bean
	public Job searchHistoryJob() {
		log.info("검색 이력 배치 작업 초기화 (파티셔닝 모드) - chunkSize: {}, skipLimit: {}, poolSize: {}, directory: {}, pattern: {}",
			chunkSize, skipLimit, poolSize, logDirectory, logPattern);

		return new JobBuilder("searchHistoryJob", jobRepository)
			.start(masterStep())
			.build();
	}

	@Bean
	public Step masterStep() {
		return new StepBuilder("masterStep", jobRepository)
			.partitioner("slaveStep", partitioner)
			.step(slaveStep())
			.taskExecutor(batchTaskExecutor())
			.build();
	}

	@Bean
	public Step slaveStep() {
		return new StepBuilder("slaveStep", jobRepository)
			.<UserBehaviorDto, UserBehaviorDto>chunk(chunkSize, transactionManager)
			.reader(partitionedReader(null))
			.writer(searchHistoryWriter())
			.faultTolerant()
			.skip(Exception.class)
			.skipLimit(skipLimit)
			.listener(skipListener)
			.build();
	}

	@Bean
	public TaskExecutor batchTaskExecutor() {
		return new TaskExecutorAdapter(
			new VirtualThreadTaskExecutor("batch-async-")
		);
	}

	/**
	 * 파티션별 파일을 처리하는 Reader
	 * @param filePath StepExecutionContext에서 주입받는 파일 경로
	 */
	@Bean
	@StepScope
	public FlatFileItemReader<UserBehaviorDto> partitionedReader(
		@Value("#{stepExecutionContext['filePath']}") String filePath) {

		String behaviorType = determineBehaviorType(filePath);
		SearchLogLineMapper lineMapper = new SearchLogLineMapper(behaviorType);
		Resource resource = resourceProvider.createResource(filePath);

		log.info("파티션 Reader 초기화 - 파일: {}, 타입: {}", filePath, behaviorType);

		return new FlatFileItemReaderBuilder<UserBehaviorDto>()
			.name("partitionedReader")
			.resource(resource)
			.lineMapper(lineMapper)
			.build();
	}

	@Bean
	public JdbcBatchItemWriter<UserBehaviorDto> searchHistoryWriter() {
		return new JdbcBatchItemWriterBuilder<UserBehaviorDto>()
			.dataSource(dataSource)
			.sql(UPSERT_SQL)
			.beanMapped()
			.build();
	}

	/**
	 * 파일 경로에서 동작 타입 결정
	 */
	private String determineBehaviorType(String filePath) {
		return (filePath != null && filePath.contains("item-view")) ? "VIEW" : "SEARCH";
	}
}
