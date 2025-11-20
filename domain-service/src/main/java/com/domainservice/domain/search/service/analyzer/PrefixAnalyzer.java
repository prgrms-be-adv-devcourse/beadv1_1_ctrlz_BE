package com.domainservice.domain.search.service.analyzer;

import org.springframework.stereotype.Component;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PrefixAnalyzer {

	private final Dictionary dictionary;

	public boolean existsInDictionary(String value) {
		//해당 영단어가 사전에 있는지
		try {
			// WordNet의 명사, 동사, 형용사, 부사 카테고리 모두에서 검색
			return dictionary.getIndexWord(net.sf.extjwnl.data.POS.NOUN, value) != null ||
				dictionary.getIndexWord(net.sf.extjwnl.data.POS.ADVERB, value) != null;
		} catch (JWNLException e) {
			// 예외 발생 시 기본적으로 false 반환 (로그를 남길 수도 있음)
			return false;
		}
	}
}
