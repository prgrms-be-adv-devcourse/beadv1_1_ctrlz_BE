package com.domainservice.domain.search.service.converter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SearchWordConverterTest {

	@Test //현재는 실패중
	void testConvertToKoreanWord() {
		assertEquals("안녕", PrefixConverter.convertToKoreanWord("dkssud"));
		assertEquals("컴퓨터", PrefixConverter.convertToKoreanWord("zjavbxj"));
		assertEquals("한성 키보드 k50", PrefixConverter.convertToKoreanWord("gkstjd zlqhem k50"));
	}

	@Test
	void testConvertToQwertyInput() {
		assertEquals("dkssud", PrefixConverter.convertToQwertyInput("안녕"));
		assertEquals("zjavbxj", PrefixConverter.convertToQwertyInput("컴퓨터"));
		assertEquals("gkstjd zlqhem k50", PrefixConverter.convertToQwertyInput("한성 키보드 k50"));
	}
}