package com.domainservice.domain.reivew.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewConstant {

	REVIEW_CREATED("상품에 대한 리뷰가 작성되었습니다.")
	;

	private final String message;
}
