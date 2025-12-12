package com.common.model.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {
	NEW("새상품"),
	GOOD("중고"),
	FAIR("사용감많음");

	private final String description;
}
