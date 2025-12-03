package com.domainservice.domain.search.service.converter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SearchWordConverterTest {

	@Test
	@DisplayName("한글을 키보드 영타 입력값으로 변환할 수 있다")
	void testConvertToKoreanWord() {
		assertEquals("안녕", PrefixConverter.convertToKoreanWord("dkssud"));
		assertEquals("컴퓨터", PrefixConverter.convertToKoreanWord("zjavbxj"));
		assertEquals("한성 키보드 k50", PrefixConverter.convertToKoreanWord("gkstjd zlqhem k50"));
	}

	@Test
	@DisplayName("키보드 영타 입력값을 정확한 한글 문자열로 변환한다")
	void testConvertToQwertyInput() {
		assertEquals("dkssud", PrefixConverter.convertToQwertyInput("안녕"));
		assertEquals("zjavbxj", PrefixConverter.convertToQwertyInput("컴퓨터"));
		assertEquals("gkstjd zlqhem k50", PrefixConverter.convertToQwertyInput("한성 키보드 k50"));
	}
}