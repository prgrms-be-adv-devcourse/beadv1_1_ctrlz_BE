package com.domainservice.common.init.dummy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DummyDataLoader {

	/**
	 * 텍스트 파일을 라인별로 읽어 리스트로 반환
	 */
	public List<String> loadLines(String resourcePath) {
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(
				new ClassPathResource(resourcePath).getInputStream(),
				StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty() && !line.startsWith("#")) {
					lines.add(line);
				}
			}
		} catch (IOException e) {
			log.error("파일 로드 실패: {}", resourcePath, e);
		}
		return lines;
	}

	/**
	 * CSV 파일을 읽어 카테고리명별 상품명 맵으로 반환
	 */
	public Map<String, List<String>> loadProductNames(String resourcePath) {
		Map<String, List<String>> productMap = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(
				new ClassPathResource(resourcePath).getInputStream(),
				StandardCharsets.UTF_8))) {
			String line;
			boolean isFirstLine = true;
			while ((line = reader.readLine()) != null) {
				if (isFirstLine) {
					isFirstLine = false;
					continue;
				}
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) continue;

				String[] parts = line.split(",", 2);
				if (parts.length == 2) {
					String categoryName = parts[0].trim();
					String productName = parts[1].trim();
					productMap.computeIfAbsent(categoryName, k -> new ArrayList<>())
						.add(productName);
				}
			}
		} catch (IOException e) {
			log.error("CSV 파일 로드 실패: {}", resourcePath, e);
		}
		return productMap;
	}

}