package com.domainservice.domain.post.post.model.dto.request;

import com.common.model.vo.ProductStatus;
import com.common.model.vo.TradeStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProductPostSearchRequest {

	@Schema(description = "카테고리 ID", example = "category-uuid-1234")
	private String categoryId;

	@Schema(description = "상품 상태")
	private ProductStatus status;

	@Schema(description = "거래 상태")
	private TradeStatus tradeStatus;

	@Schema(description = "최소 가격")
	@Min(value = 0, message = "최소 가격은 0원 이상이어야 합니다.")
	private Integer minPrice;

	@Schema(description = "최대 가격")
	@Min(value = 0, message = "최대 가격은 0원 이상이어야 합니다.")
	private Integer maxPrice;

}
