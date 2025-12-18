package com.search.scheduler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Qualifier;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SearchBatchJobLauncher extends QuartzJobBean {

	private final JobLauncher jobLauncher;
	private final Job searchHistoryJob;

	// Settlement 배치가 실행되는 날짜 (매월 15일)
	private static final int SETTLEMENT_DAY = 15;

	public SearchBatchJobLauncher(
			JobLauncher jobLauncher,
			@Qualifier("searchHistoryJob") Job searchHistoryJob) {
		this.jobLauncher = jobLauncher;
		this.searchHistoryJob = searchHistoryJob;
	}

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

		LocalDateTime now = LocalDateTime.now();
		int dayOfMonth = now.getDayOfMonth();

		// Settlement 배치가 실행되는 날(매월 15일)에는 Search 배치를 실행하지 않음
		if (dayOfMonth == SETTLEMENT_DAY) {
			log.info("오늘은 Settlement 배치 실행일({}일)입니다. Search 배치를 건너뜁니다.", SETTLEMENT_DAY);
			return;
		}

		log.info("Quartz 스케줄러에 의해 배치 작업 시작: {}", now);
		try {
			JobParameters jobParameters = new JobParametersBuilder()
					.addString("executedAt",
							LocalDateTime.parse(now.toString(), DateTimeFormatter.ISO_DATE_TIME).toString())
					.toJobParameters();

			jobLauncher.run(searchHistoryJob, jobParameters);
		} catch (Exception e) {
			log.error("배치 작업 실행 실패: {}", e.getMessage(), e);
			throw new JobExecutionException("배치 실행 실패", e);
		}
	}
}
