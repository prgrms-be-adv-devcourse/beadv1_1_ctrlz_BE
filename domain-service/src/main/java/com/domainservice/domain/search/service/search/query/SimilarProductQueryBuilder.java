package com.domainservice.domain.search.service.search.query;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Component;

import com.domainservice.domain.search.model.entity.dto.document.ProductPostDocumentEntity;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MoreLikeThisQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;

@Component
public class SimilarProductQueryBuilder {

	/**
	 * 유사 상품 검색 쿼리 생성
	 * - More Like This (기본 유사도) + Should (정확도 보정)
	 */
	public NativeQuery build(ProductPostDocumentEntity baseProduct, Pageable pageable) {

		// More Like This 쿼리 (유사도 기반 검색)
		Query mltQuery = buildMoreLikeThisQuery(baseProduct);

		// Should 절 (정확도 보정)
		List<Query> shouldQueries = buildShouldQueries(baseProduct);

		// Bool Query로 조합
		Query similarQuery = Query.of(q -> q.bool(
			BoolQuery.of(bool -> bool
				.must(mltQuery)
				.should(shouldQueries)
				.minimumShouldMatch("1")  // Should 중 최소 1개는 매칭
				.filter(
					List.of(
						Query.of(filter -> filter.bool(b -> b
							.mustNot(
								Query.of(mustNot -> mustNot.term(term -> term.field("_id").value(baseProduct.getId())))
							))),
						Query.of(filter -> filter.term(term -> term.field("delete_status").value("N")
						))
					))
			)
		));

		return NativeQuery.builder()
			.withQuery(similarQuery)
			.withPageable(pageable)
			.build();

	}

	/**
	 * More Like This 쿼리 생성
	 */
	private Query buildMoreLikeThisQuery(ProductPostDocumentEntity baseProduct) {
		return Query.of(q -> q.moreLikeThis(
			MoreLikeThisQuery.of(mlt -> mlt
				.fields(List.of(
					"name",
					"title",
					"description",
					"category_name"
				))
				.like(like -> like.document(doc -> doc
					.index("product-posts")
					.id(baseProduct.getId())
				))
				// 키워드 추출 기준
				.minTermFreq(1)                // 기준 문서 (like) 안에서 최소 1번만 등장해도 키워드 후보로 추출
				.minDocFreq(1)                 // es의 전체 DB에서 1번만 등장해도 유효한 키워드로 인정
				.maxQueryTerms(25)             // 추출된 후보 중 점수가 높은 상위 25개만 실제 검색 쿼리로 사용
				.minimumShouldMatch("20%")     // 사용된 25개 키워드 중 20% 이상 일치하는 문서만 결과로 반환
			)
		));
	}

	/**
	 * Should 절 생성
	 * - More Like This를 보완하는 정확도 보정 역할
	 * - 상품명, 카테고리, 태그에 가중치 부여
	 */
	private List<Query> buildShouldQueries(ProductPostDocumentEntity baseProduct) {
		List<Query> shouldQueries = new ArrayList<>();

		// name과 정확히 일치하면 높은 가중치 부여
		shouldQueries.add(Query.of(q -> q.match(m -> m
			.field("name.keyword")
			.query(baseProduct.getName())
			.boost(10.0f)
		)));

		// 상품명 전체 매치 (정확도 높임)
		if (baseProduct.getName() != null && !baseProduct.getName().isEmpty()) {
			shouldQueries.add(Query.of(q -> q.multiMatch(
				MultiMatchQuery.of(mm -> mm
					.query(baseProduct.getName())
					.fields(List.of(
						"name^3",
						"name.ngram^2",
						"title^4",
						"title.ngram^2",
						"description^2"
					))
					.type(TextQueryType.BestFields)
					.fuzziness("AUTO")
					.boost(5.0f)
				)
			)));
		}

		// 카테고리 매치
		if (baseProduct.getCategoryName() != null) {
			shouldQueries.add(Query.of(q -> q.match(m -> m
				.field("category_name")
				.query(baseProduct.getCategoryName())
				.boost(5.0f)
			)));
		}

		// 태그 매치
		if (baseProduct.getTags() != null && !baseProduct.getTags().isEmpty()) {
			shouldQueries.add(Query.of(q -> q.terms(t -> t
				.field("tags")
				.terms(terms -> terms.value(
					baseProduct.getTags().stream()
						.map(FieldValue::of)
						.toList()
				))
				.boost(2.0f)
			)));
		}

		return shouldQueries;
	}
}