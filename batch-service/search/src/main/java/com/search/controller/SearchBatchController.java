package com.search.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Qualifier;

import lombok.extern.slf4j.Slf4j;
/**
 * 특정 ip만 허용합니다.
 */
@Slf4j
@RestController
@RequestMapping("/batch/search")
public class SearchBatchController {

	private final JobLauncher jobLauncher;
	private final Job searchHistoryJob;

	public SearchBatchController(
			JobLauncher jobLauncher,
			@Qualifier("searchHistoryJob") Job searchHistoryJob) {
		this.jobLauncher = jobLauncher;
		this.searchHistoryJob = searchHistoryJob;
	}

	/**
	 * 검색 이력 배치 수동 실행
	 */
	@PostMapping("/manual")
	public String runSearchBatch() {
		LocalDateTime now = LocalDateTime.now();
		log.info("수동 배치 실행 요청: {}", now);
		try {
			JobParameters jobParameters = new JobParametersBuilder()
					.addString("manualExecutedAt",
							LocalDateTime.parse(now.toString(), DateTimeFormatter.ISO_DATE_TIME).toString())
					.toJobParameters();

			jobLauncher.run(searchHistoryJob, jobParameters);
			return "배치 작업이 성공적으로 시작되었습니다.";
		} catch (Exception e) {
			log.error("수동 배치 실행 실패", e);
			throw new RuntimeException(e);
		}
	}
}
