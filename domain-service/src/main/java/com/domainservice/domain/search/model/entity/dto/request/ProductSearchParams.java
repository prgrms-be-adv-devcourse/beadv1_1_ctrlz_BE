package com.domainservice.domain.search.model.entity.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 검색 요청 파라미터")
public record ProductSearchParams(

	@Schema(description = "검색어", example = "아이폰")
	String q,

	@Schema(description = "카테고리", example = "전자기기")
	String category,

	@Schema(description = "최소 가격", example = "100000", defaultValue = "0")
	Long minPrice,

	@Schema(description = "최대 가격", example = "2000000", defaultValue = "999999999")
	Long maxPrice,

	@Schema(description = "태그 (콤마로 구분)", example = "친환경,중고")
	String tags,

	@Schema(
		description = "상품 상태", example = "NEW",
		allowableValues = {"NEW", "GOOD", "ALL"},
		defaultValue = "ALL"
	)
	String status,

	@Schema(
		description = "판매 상태", example = "SELLING",
		allowableValues = {"SELLING", "ALL"},
		defaultValue = "ALL"
	)
	String tradeStatus,

	@Schema(
		description = "정렬 기준", example = "score",
		allowableValues = {"score", "popular", "price_asc", "price_desc", "newest"},
		defaultValue = "score"
	)
	String sort
) {
	public ProductSearchParams {
		minPrice = minPrice != null ? minPrice : 0L;
		maxPrice = maxPrice != null ? maxPrice : 999999999L;
		status = status != null && !status.isBlank() ? status : "ALL";
		tradeStatus = tradeStatus != null && !tradeStatus.isBlank() ? tradeStatus : "ALL";
		sort = sort != null && !sort.isBlank() ? sort : "score";
	}
}
