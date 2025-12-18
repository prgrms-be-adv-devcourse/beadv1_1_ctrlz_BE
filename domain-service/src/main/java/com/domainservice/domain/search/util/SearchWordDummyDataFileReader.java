package com.domainservice.domain.search.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class SearchWordDummyDataFileReader {

	private static final String FILE_PATH = "dummy/used_market_search_terms_50k.txt";
	private final List<String> cachedLines;

	public SearchWordDummyDataFileReader() {
		try(InputStream inputStream = new ClassPathResource(FILE_PATH).getInputStream()) {
			cachedLines = StreamUtils.copyToString(
				inputStream,
				StandardCharsets.UTF_8
			).lines().toList();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> readSearchWordFromFile(int count) {
		Random random = new Random();
		List<String> result = new ArrayList<>(count);

		for (int i = 0; i < count; i++) {
			result.add(cachedLines.get(random.nextInt(cachedLines.size())));
		}

		return result;
	}
}
