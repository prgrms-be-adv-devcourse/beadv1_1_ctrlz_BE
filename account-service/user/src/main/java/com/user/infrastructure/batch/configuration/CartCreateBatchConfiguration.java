package com.user.infrastructure.batch.configuration;

import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.user.application.adapter.dto.CartCreateCommand;
import com.user.infrastructure.batch.CartCreateWriter;
import com.user.infrastructure.jpa.entity.ExternalEventEntity;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CartCreateBatchConfiguration {

	private final EntityManagerFactory entityManagerFactory;
	private final CartCreateWriter batchCartCreateItemWriter;


	@Bean
	public Job cartCreateJob(Step cartCreateStep, JobRepository jobRepository) {
		return new JobBuilder("cartCreateJob", jobRepository)
			.start(cartCreateStep)
			.build();
	}

	@Bean
	public Step cartCreateStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("cartCreateStep", jobRepository)
			.<ExternalEventEntity, CartCreateCommand>chunk(1000, transactionManager)
			.reader(cartCreateItemReader())
			.processor(cartCreateItemProcessor())
			.writer(cartCreateItemWriter())
			.build();
	}

	@Bean
	public JpaCursorItemReader<ExternalEventEntity> cartCreateItemReader(){
		log.info("Creating cart reader for cart");
		return new JpaCursorItemReaderBuilder<ExternalEventEntity>()
			.name("ExternalEventEntityItemReader")
			.entityManagerFactory(entityManagerFactory)
			.queryString("select e from ExternalEventEntity e where published =: status order by e.id asc")
			.parameterValues(Map.of("status", false))
			.build();
	}

	@Bean
	public ItemProcessor<ExternalEventEntity, CartCreateCommand> cartCreateItemProcessor() {
		return item -> new CartCreateCommand(item.getUserId());
	}

	@Bean
	public ItemWriter<CartCreateCommand> cartCreateItemWriter() {
		return batchCartCreateItemWriter;
	}

}
