package com.domainservice.domain.reivew.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewConstant {

	REVIEW_CREATED("상품에 대한 리뷰가 작성되었습니다."),
	REVIEW_FETCHED("리뷰 데이터를 가져왔습니다."),
	REVIEW_UPDATED("리뷰 데이터를 수정했습니다.")
	;

	private final String message;
}
