package com.user.infrastructure.batch.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartCreateBatchScheduler {

	private final JobLauncher jobLauncher;
	private final Job cartCreateJob;

	@Scheduled(cron = "0 */5 * * * *")
	public void runCartCreateBatch() {
		try {
			JobParameters jobParameters = new JobParametersBuilder()
				.addLong("time", System.currentTimeMillis())
				.toJobParameters();

			JobExecution jobExecution = jobLauncher.run(cartCreateJob, jobParameters);

			if (jobExecution.getStatus().isUnsuccessful()) {
				log.error("CartCreateBatch job failed with status: {}", jobExecution.getStatus());
			}

			log.info("CartCreateBatch job completed successfully");

		} catch (Exception e) {
			log.error("Error running CartCreateBatch job", e);
		}
	}
}
