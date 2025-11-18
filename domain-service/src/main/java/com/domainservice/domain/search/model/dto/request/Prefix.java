package com.domainservice.domain.search.model.dto.request;

import com.domainservice.domain.search.service.converter.SearchWordConverter;

public record Prefix(
	String value
) {
	public String getQwertyInput() {
		return SearchWordConverter.convertToQwertyInput(this.value);
	}

	public String getKoreanWord() {
		return SearchWordConverter.convertToKoreanWord(this.value);
	}

	public boolean isEnglish() {
		return this.value.replaceAll("[a-z]", "").isEmpty();
	}
}
