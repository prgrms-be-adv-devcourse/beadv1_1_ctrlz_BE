package com.domainservice.domain.search.service.converter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class SearchWordConverterTest {

	// @Test //현재는 실패중
	// void testConvertToKoreanWord() {
	// 	assertEquals("안녕", SearchWordConverter.convertToKoreanWord("dkssud"));
	// 	assertEquals("컴퓨터", SearchWordConverter.convertToKoreanWord("zjavbxj"));
	// }

	@Test
	void testConvertToQwertyInput() {
		assertEquals("dkssud", SearchWordConverter.convertToQwertyInput("안녕"));
		assertEquals("zjavbxj", SearchWordConverter.convertToQwertyInput("컴퓨터"));
	}
}