package com.domainservice.domain.search.service.dto.request;

import com.domainservice.domain.search.service.converter.PrefixConverter;

public record Prefix(
	String value
) {
	public String getQwertyInput() {
		return PrefixConverter.convertToQwertyInput(this.value);
	}

	public String getKoreanWord() {
		return PrefixConverter.convertToKoreanWord(this.value);
	}

	public boolean isEnglish() {
		return this.value.replaceAll("[a-z]", "").isEmpty();
	}
}
