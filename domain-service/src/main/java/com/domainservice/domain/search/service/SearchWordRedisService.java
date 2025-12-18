package com.domainservice.domain.search.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.domainservice.domain.search.model.dto.response.SearchWordResponse;
import com.domainservice.domain.search.model.vo.SearchWord;
import com.domainservice.domain.search.repository.redis.PopularSearchWordRedisRepository;
import com.domainservice.domain.search.repository.redis.SearchLogRedisRepository;
import com.domainservice.domain.search.service.converter.PrefixConverter;
import com.domainservice.domain.search.service.kafka.producer.SearchWordEventProducer;
import com.domainservice.domain.search.util.SearchWordDummyDataFileReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchWordRedisService {

	private final SearchWordEventProducer searchWordEventProducer;

	private final SearchLogRedisRepository searchLogRedisRepository;
	private final PopularSearchWordRedisRepository popularRedisRepository;
	private final SearchWordDummyDataFileReader searchWordDummyDataFileReader;

	@Transactional(readOnly = true)
	public List<SearchWordResponse> getRecentSearchWordList(String userId) {
		return searchLogRedisRepository.findByUserKey(userId).stream()
			.map(log -> new SearchWordResponse(
				log.value(),
				PrefixConverter.convertToQwertyInput(log.value())
			))
			.toList();
	}

	@Transactional(readOnly = true)
	public List<SearchWordResponse> getTrendWordList() {
		return popularRedisRepository.findTrendWordList();
	}

	@Transactional(readOnly = true)
	public List<SearchWordResponse> getDailyPopularWord() {
		return popularRedisRepository.findDailyPopularWord();
	}

	/**
	 * 검색창에 입력한 단어 or 자동완성으로 나온 단어 선택 후 검색 결과 화면에서 보여지는 단어 저장하는 메서드.
	 * @param word 검색창에 입력한 단어
	 * @param userId user PK
	 */
	@Transactional
	public void saveSearchWord(String word, String userId) {
		SearchWord searchWord = new SearchWord(word);

		if(userId != null) {
			searchLogRedisRepository.save(searchWord, userId);
			searchWordEventProducer.publishSearchWordEventToAi(word, userId);
		}

		popularRedisRepository.save(searchWord);
	}

	public void saveSearchWordForTest() {
		List<String> list = searchWordDummyDataFileReader.readSearchWordFromFile(40000);
		for (String word : list) {
			SearchWord searchWord = new SearchWord(word);
			popularRedisRepository.save(searchWord);
		}
	}
}
