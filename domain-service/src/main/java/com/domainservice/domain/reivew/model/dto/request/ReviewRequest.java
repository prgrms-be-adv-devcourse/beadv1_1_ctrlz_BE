package com.domainservice.domain.reivew.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReviewRequest(

	@NotBlank(message = "필수 파라미터 누락 - 상품id")
	String productId,

	@NotBlank(message = "내용은 필수 입력값입니다.")
	String contents,

	@Min(value = 1, message = "유효하지 않은 사용자 평점입니다.")
	@Max(value = 5, message = "유효하지 않은 사용자 평점입니다.")
	Integer userRating,

	@Min(value = 1, message = "유효하지 않은 상품 평점입니다.")
	@Max(value = 5, message = "유효하지 않은 상품 평점입니다.")
	Integer productRating
) {
}
