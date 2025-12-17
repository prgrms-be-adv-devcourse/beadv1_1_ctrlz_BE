package com.domainservice.domain.post.post.model.dto.request;

import com.common.model.vo.ProductStatus;
import com.common.model.vo.TradeStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

public record ProductPostSearchRequest(
	@Schema(description = "카테고리 ID (optional)", example = "category-uuid-1234")
	String categoryId,

	@Schema(description = "상품 상태 (optional)")
	ProductStatus status,

	@Schema(description = "거래 상태 (optional)")
	TradeStatus tradeStatus,

	@Schema(description = "최소 가격 (optional)")
	@Min(value = 0, message = "최소 가격은 0원 이상이어야 합니다.")
	Integer minPrice,

	@Schema(description = "최대 가격 (optional)")
	@Min(value = 0, message = "최대 가격은 0원 이상이어야 합니다.")
	Integer maxPrice
) {
	public ProductPostSearchRequest {
		if (minPrice == null) minPrice = 0;
		if (maxPrice == null) maxPrice = Integer.MAX_VALUE;
	}
}
