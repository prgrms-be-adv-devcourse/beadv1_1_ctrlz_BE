package com.domainservice.domain.search.service.search.query;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Component;

import com.domainservice.domain.search.model.entity.dto.document.ProductPostDocumentEntity;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

@Component
public class SellerProductQueryBuilder {

	/**
	 * 특정 상품의 판매자가 판매하고 있는 다른 상품 목록 조회 쿼리 생성
	 */
	public NativeQuery build(ProductPostDocumentEntity baseProduct, Pageable pageable) {

		Query sellerProductQuery = Query.of(q -> q.bool(
			BoolQuery.of(bool -> bool
				.must(
					Query.of(m -> m.term(t -> t.field("user_id").value(baseProduct.getUserId())))
				)
				.filter(
					List.of(
						Query.of(
							f -> f.bool(b -> b.mustNot(mn -> mn.term(t -> t.field("_id").value(baseProduct.getId()))))),
						Query.of(f -> f.term(t -> t.field("delete_status").value("N"))),
						Query.of(f -> f.term(t -> t.field("trade_status").value("SELLING")))
					)
				)
			)
		));

		return NativeQuery.builder()
			.withQuery(sellerProductQuery)
			.withSort(sort -> sort.field(f -> f.field("created_at").order(SortOrder.Desc)))
			.withPageable(pageable)
			.build();
	}
}