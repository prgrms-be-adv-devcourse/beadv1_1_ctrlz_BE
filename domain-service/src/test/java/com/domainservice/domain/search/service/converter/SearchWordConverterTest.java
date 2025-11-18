package com.domainservice.domain.search.service.converter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SearchWordConverterTest {

	@Test //현재는 실패중
	void testConvertToKoreanWord() {
		assertEquals("안녕", SearchWordConverter.convertToKoreanWord("dkssud"));
		assertEquals("컴퓨터", SearchWordConverter.convertToKoreanWord("zjavbxj"));
		assertEquals("한성 키보드 k50", SearchWordConverter.convertToKoreanWord("gkstjd zlqhem k50"));
	}

	@Test
	void testConvertToQwertyInput() {
		assertEquals("dkssud", SearchWordConverter.convertToQwertyInput("안녕"));
		assertEquals("zjavbxj", SearchWordConverter.convertToQwertyInput("컴퓨터"));
		assertEquals("gkstjd zlqhem k50", SearchWordConverter.convertToQwertyInput("한성 키보드 k50"));
	}
}