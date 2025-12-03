package com.domainservice.domain.search.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.domainservice.domain.search.service.SearchWordSchedulerService;
import com.domainservice.domain.search.service.dto.vo.DailyPopularWordLog;
import com.domainservice.domain.search.service.dto.vo.KeywordLog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchWordScheduler {

	private final ReentrantLock lock = new ReentrantLock();
	private final Condition realtimeDone = lock.newCondition();

	private boolean realtimeSynced = false;

	private final SearchWordSchedulerService batchService;
	private final Executor batchExecutor;

	/**
	 * 5분 마다 trendScore를 다시 계산 해서 실시간 인기 검색어를 업데이트. + DB최신화
	 */
	@Scheduled(cron = "0 */5 * * * *")
	public void syncRealTimeTrends() {
		lock.lock();
		LocalDateTime now = LocalDateTime.now();
		try {
			List<KeywordLog> logs = batchService.getTrendWordLogList();

			CompletableFuture<Void> updateTrendFuture =
				CompletableFuture.runAsync(() ->
						batchService.updateRealtimeTrendWordList(logs),
					batchExecutor
				).thenAccept(result -> log.info("실시간 트렌드 검색어 업데이트 완료"));

			CompletableFuture<Void> saveDBFuture =
				CompletableFuture.runAsync(() ->
						batchService.saveLogsToDataBase(logs),
					batchExecutor
				).thenAccept(result -> log.info("DB 저장 완료"));

			CompletableFuture.allOf(updateTrendFuture, saveDBFuture)
				.exceptionally(ex -> {
					log.error("Error during syncRealTimeTrends", ex);
					return null;
				});

			realtimeSynced = true;             // A 완료 표시
			realtimeDone.signalAll();          // B 깨우기
		} finally {
			//2시, 4시, 6시, ..., 22시 정각이 아니면
			if(!(now.getHour() % 2 == 0 && now.getMinute() == 0)) {
				realtimeSynced = false;
			}

			lock.unlock();
		}

	}

	/**
	 * 0시부터 2시간마다 일간 인기 검색어 업데이트 + Elasticsearch
	 */
	@Scheduled(cron = "0 0 */2 * * *")
	public void updateDailyPopularAndElasticsearch() {
		lock.lock();

		try {
			while (!realtimeSynced) {
				realtimeDone.await();        // A가 signal할 때까지 무제한 대기
			}

			log.info("A 완료됨 → B 작업 시작");

			List<KeywordLog> keywordCountLogList = batchService.collectDailyPopularRedisLog();
			Map<String, DailyPopularWordLog> previousLogMap = batchService.getPreviousDailyPopularDBLog();

			batchService.updateDailyPopularWordList(keywordCountLogList, previousLogMap);


		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			lock.unlock();
		}
	}
}