package com.search.configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 로그 파일을 파티션으로 분할하는 Partitioner
 * 각 파일을 독립적인 파티션으로 처리하여 병렬 실행 지원
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchFilePartitioner implements Partitioner {

	private final SearchLogResourceProvider resourceProvider;

	@Value("${batch.search.log-directory:logs}")
	private String logDirectory;

	@Value("${batch.search.log-pattern:item-view.log*,search-view.log*}")
	private String logPattern;

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		Resource[] resources = resourceProvider.createResources(logDirectory, logPattern);
		Map<String, ExecutionContext> partitions = new HashMap<>();

		// gridSize 고려한 실제 파티션 수 계산
		int actualPartitionCount = Math.min(resources.length, gridSize);

		log.info("파티셔닝 시작 - 파일 수: {}, gridSize: {}, 실제 파티션: {}",
				resources.length, gridSize, actualPartitionCount);

		for (int i = 0; i < resources.length; i++) {
			ExecutionContext context = new ExecutionContext();
			try {
				String filePath = resources[i].getFile().getAbsolutePath();
				context.putString("filePath", filePath);
				context.putString("fileName", resources[i].getFilename());
				partitions.put("partition" + i, context);

				log.debug("파티션 생성: partition{} -> {}", i, filePath);
			} catch (IOException e) {
				log.error("파일 경로 추출 실패: {}", resources[i].getDescription(), e);
			}
		}

		log.info("파티셔닝 완료 - 생성된 파티션 수: {}", partitions.size());
		return partitions;
	}
}
