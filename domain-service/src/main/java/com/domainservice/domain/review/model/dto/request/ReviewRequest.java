package com.domainservice.domain.review.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReviewRequest(

	@Schema(description = "리뷰를 작성할 상품(게시글) ID", example = "post-uuid-1234")
	@NotBlank(message = "필수 파라미터 누락 - 상품id")
	String productId,

	@Schema(description = "리뷰 내용", example = "배송도 빠르고 상품 상태도 설명과 같아서 좋았어요!")
	@NotBlank(message = "내용은 필수 입력값입니다.")
	String contents,

	@Schema(description = "판매자 평점 (1~5점)", example = "5", minimum = "1", maximum = "5")
	@Min(value = 1, message = "유효하지 않은 사용자 평점입니다.")
	@Max(value = 5, message = "유효하지 않은 사용자 평점입니다.")
	Integer userRating,

	@Schema(description = "상품 평점 (1~5점)", example = "4", minimum = "1", maximum = "5")
	@Min(value = 1, message = "유효하지 않은 상품 평점입니다.")
	@Max(value = 5, message = "유효하지 않은 상품 평점입니다.")
	Integer productRating
) {
}
