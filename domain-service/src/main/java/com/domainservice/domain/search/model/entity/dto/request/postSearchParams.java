package com.domainservice.domain.search.model.entity.dto.request;

import static com.common.exception.vo.ProductPostExceptionCode.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.domainservice.domain.search.exception.ElasticSearchException;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

@Schema(description = "상품 검색 요청 파라미터")
public record postSearchParams(

	@Schema(description = "검색어", example = "아이폰")
	String q,

	@Schema(description = "카테고리", example = "모바일/태블릿")
	String category,

	@Schema(description = "최소 가격", defaultValue = "0")
	@Min(value = 0, message = "최소 가격은 0원 이상이어야 합니다.")
	Long minPrice,

	@Schema(description = "최대 가격", defaultValue = "100000000")
	@Min(value = 0, message = "최대 가격은 0원 이상이어야 합니다.")
	Long maxPrice,

	@Schema(description = "태그 (콤마로 구분)", example = "미개봉,무료배송")
	String tags,

	@Schema(
		description = """
           상품 상태
           - `NEW`: 새 상품
           - `GOOD`: 중고 상품
           - `ALL`: 전체 (default)
           """,
		defaultValue = "ALL"
	)
	@Pattern(regexp = "^(NEW|GOOD|FAIR|ALL)$", message = "상품 상태 형식이 올바르지 않습니다.")
	String status,

	@Schema(
		description = """
           거래 상태
           - `SELLING`: 판매중
           - `ALL`: 전체 (default)
           """,
		defaultValue = "ALL"
	)
	@Pattern(regexp = "^(SELLING|SOLDOUT|PROCESSING|ALL)$", message = "거래 상태 형식이 올바르지 않습니다.")
	String tradeStatus,

	@Schema(
		description = """
           정렬 기준
           - `score`: 관련도 순 (default)
           - `popular`: 인기순 (좋아요+조회수)
           - `price_asc`: 가격 낮은 순
           - `price_desc`: 가격 높은 순
           - `newest`: 최신 등록순
           """,
		defaultValue = "score"
	)
	@Pattern(regexp = "^(score|popular|price_asc|price_desc|newest)$", message = "정렬 기준 값이 올바르지 않습니다.")
	String sort
) {
	public postSearchParams {
		if (minPrice == null) minPrice = 0L;
		if (maxPrice == null) maxPrice = 100_000_000L;
		if (status == null) status = "ALL";
		if (tradeStatus == null) tradeStatus = "ALL";
		if (sort == null) sort = "score";

		if (minPrice > maxPrice) {
			throw new ElasticSearchException(INVALID_PRICE_RANGE);
		}

	}

	public List<String> getParsedTagList() {
		if (!this.hasTags()) return new ArrayList<>();

		return Arrays.stream(tags.split(","))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.toList();
	}

	public boolean hasQuery() {return q != null && !q.isBlank();}
	public boolean hasCategory() {return category != null && !category.isBlank();}
	public boolean hasTags() {return tags != null && !tags.isEmpty();}
	public boolean hasStatus() {return !status.equals("ALL");}
	public boolean hasTradeStatus() {return !tradeStatus.equals("ALL");}

}
