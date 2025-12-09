package com.search.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchLogResourceProvider {

	public Resource[] createResources(String directory, String pattern) {
		Path dirPath = Paths.get(directory);

		// 디렉토리 유효성 검증 - 유효하지 않으면 빈 배열 반환
		if (!validateDirectory(dirPath, directory)) {
			return new Resource[0];
		}

		String[] patterns = parsePatterns(pattern);
		List<Resource> resources = collectResources(dirPath, patterns, directory, pattern);

		return resources.toArray(new Resource[0]);
	}

	/**
	 * 디렉토리 유효성 검증
	 * 
	 * @return 유효한 디렉토리인 경우 true, 아니면 false (빈 배열 반환용)
	 */
	private boolean validateDirectory(Path dirPath, String directory) {
		if (!Files.exists(dirPath)) {
			log.warn("디렉토리가 존재하지 않습니다: {}", directory);
			return false;
		}
		if (!Files.isDirectory(dirPath)) {
			log.warn("디렉토리가 아닙니다: {}", directory);
			return false;
		}
		return true;
	}

	/**
	 * 패턴 문자열 파싱 (쉼표로 구분된 여러 패턴 지원)
	 */
	private String[] parsePatterns(String pattern) {
		return pattern.contains(",")
				? pattern.split(",")
				: new String[] { pattern };
	}

	/**
	 * 패턴에 맞는 리소스 수집
	 */
	private List<Resource> collectResources(Path dirPath, String[] patterns,
			String directory, String pattern) {
		List<Resource> resources = new ArrayList<>();

		for (String singlePattern : patterns) {
			try {
				List<Resource> patternResources = collectResourcesForPattern(dirPath, singlePattern.trim());
				resources.addAll(patternResources);
			} catch (IOException e) {
				log.error("디렉토리 읽기 실패: {} (패턴: {})", directory, singlePattern.trim(), e);
				// 특정 패턴 실패 시에도 다른 패턴 계속 처리
				// 모든 패턴 실패 시에만 빈 결과 반환
			}
		}

		log.info("총 {} 개의 로그 파일을 찾았습니다. 디렉토리: {}, 패턴: {}",
				resources.size(), directory, pattern);

		return resources;
	}

	/**
	 * 단일 패턴에 대한 리소스 수집
	 */
	private List<Resource> collectResourcesForPattern(Path dirPath, String trimmedPattern) throws IOException {
		List<Resource> resources = new ArrayList<>();

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, trimmedPattern)) {
			List<Path> sortedPaths = new ArrayList<>();
			stream.forEach(sortedPaths::add);

			for (Path filePath : sortedPaths) {
				if (Files.isRegularFile(filePath) && Files.isReadable(filePath)) {
					resources.add(createResource(filePath.toString()));
					log.info("로그 파일 추가: {}", filePath);
				}
			}
		}

		return resources;
	}

	/**
	 * 주어진 경로에서 Resource를 생성
	 * .gz 확장자인 경우 GZIP 압축 해제
	 *
	 * @param logPath 로그 파일 경로
	 * @return Resource 객체
	 */
	public Resource createResource(String logPath) {
		validateLogFile(logPath);

		if (logPath.endsWith(".gz")) {
			return createGzipResource(logPath);
		}
		return new FileSystemResource(logPath);
	}

	/**
	 * 파일 존재 여부와 읽기 권한을 검증
	 */
	private void validateLogFile(String path) {
		File file = new File(path);
		if (!file.exists()) {
			throw new IllegalArgumentException("로그 파일을 찾을 수 없습니다: " + path);
		}
		if (!file.canRead()) {
			throw new IllegalArgumentException("로그 파일을 읽을 수 없습니다: " + path);
		}
		log.debug("로그 파일 검증 완료: {}", path);
	}

	/**
	 * GZIP 압축 파일을 위한 Resource 생성
	 */
	private Resource createGzipResource(String logPath) {
		return new FileSystemResource(logPath) {
			@Override
			public InputStream getInputStream() throws IOException {
				return new GZIPInputStream(super.getInputStream());
			}

			@Override
			public String getDescription() {
				return "GZIP resource [" + logPath + "]";
			}
		};
	}
}
