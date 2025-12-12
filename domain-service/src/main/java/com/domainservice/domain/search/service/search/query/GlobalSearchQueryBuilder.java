package com.domainservice.domain.search.service.search.query;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Component;

import com.domainservice.domain.search.model.entity.dto.request.ProductPostSearchRequest;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;

@Component
public class GlobalSearchQueryBuilder {

	/**
	 * request로 들어온 검색어 및 필더를 기반으로 통합 검색 쿼리 생성
	 */
	public NativeQuery build(ProductPostSearchRequest request, Pageable pageable) {

		// Bool Query 생성
		Query boolQuery = buildBoolQuery(request);

		// request로 들어온 정렬 적용
		Sort sort = createSort(request.sort());

		Pageable pageableWithSort = PageRequest.of(
			pageable.getPageNumber(),
			pageable.getPageSize(),
			sort
		);

		// 생성된 boolQuery와 페이지 정보를 NativeQuery로 합쳐서 반환
		return NativeQuery.builder()
			.withQuery(boolQuery)
			.withPageable(pageableWithSort)
			.build();

	}

	// Bool Query 생성
	private Query buildBoolQuery(ProductPostSearchRequest request) {

		// must 쿼리 생성
		// 검색 점수에 반영되는 필수 조건 영역, 내부 필드가 반드시 만족해야 문서가 검색됨
		List<Query> mustQueries = new ArrayList<>();

		// multi_match 쿼리 생성
		if (request.hasQuery()) {
			mustQueries.add(buildMultiMatchQuery(request.q())); // 검색어가 있을 때 Multi-Match 수행
		} else {
			mustQueries.add(Query.of(q -> q.matchAll(m -> m))); // 검색어가 없을 때는 전체 조회 (Match All)
		}

		// filter: request로 입력된 필터 조건들 적용
		List<Query> filterQueries = new ArrayList<>();

		// 삭제되지 않은 상품만 필터
		filterQueries.add(
			Query.of(queryBuilder -> queryBuilder.term(
				TermQuery.of(termQueryBuilder -> termQueryBuilder
					.field("delete_status")
					.value("N")
				)
			))
		);

		// 카테고리 필터가 존재하면 적용
		if (request.hasCategory()) {
			filterQueries.add(
				Query.of(queryBuilder -> queryBuilder.match(
					MatchQuery.of(matchQueryBuilder -> matchQueryBuilder
						.field("category_name")
						.query(request.category())
					)
				))
			);
		}

		/*
		가격 범위 필터가 존재하면 적용 (default: 0 ~ 999999999)
		gte: greater than or equal, 이 값 이상(≥)
		lte: less than or equal, 이 값 이하(≤)
		 */
		filterQueries.add(
			Query.of(queryBuilder -> queryBuilder.range(
				rangeQueryBuilder -> rangeQueryBuilder.number(
					numberRangeBuilder -> numberRangeBuilder
						.field("price")
						.gte(Double.valueOf(request.minPrice()))
						.lte(Double.valueOf(request.maxPrice()))
				)
			))
		);

		// 태그 필터가 존재하면 적용
		if (request.hasTags()) {
			for (String tag : request.tags()) {
				filterQueries.add(
					Query.of(queryBuilder -> queryBuilder.match(
						MatchQuery.of(matchQueryBuilder -> matchQueryBuilder
							.field("tags")
							.query(tag)
						)
					))
				);
			}
		}

		// 상품 상태 필터가 존재하면 적용 ( "ALL"(default), "NEW", "GOOD" )
		if (request.hasStatus()) {
			filterQueries.add(
				Query.of(queryBuilder -> queryBuilder.term(
					TermQuery.of(termQueryBuilder -> termQueryBuilder
						.field("status")
						.value(request.status())
					)
				))
			);
		}

		// 상품 판매 상태 필터가 존재하면 적용 ( "SELLING"(default), "ALL" )
		if (!request.tradeStatus().equals("ALL")) {
			filterQueries.add(
				Query.of(queryBuilder -> queryBuilder.term(
					TermQuery.of(termQueryBuilder -> termQueryBuilder
						.field("trade_status")
						.value(request.tradeStatus())
					)
				))
			);
		}

		// 적용된 must와 filter를 Bool Query로 반환
		return Query.of(queryBuilder -> queryBuilder.bool(
			BoolQuery.of(boolQueryBuilder -> boolQueryBuilder
				.must(mustQueries)
				.filter(filterQueries)
			)
		));
	}

	// Multi-Match 쿼리 생성 (검색어)
	// 하나의 검색어를 여러 필드(name, title, description 등)에 동시에 매칭시키는 검색 쿼리
	private Query buildMultiMatchQuery(String queryString) {
		return Query.of(queryBuilder -> queryBuilder.multiMatch(
			MultiMatchQuery.of(multiMatchBuilder -> multiMatchBuilder
				.query(queryString)
				.fields(List.of(
					"name^5",          // 상품명 (최고 가중치 5)
					"name.ngram^3",    // 상품명 부분 일치
					"title^4",         // 제목
					"title.ngram^2",   // 제목 부분 일치
					"description^1.5", // 설명
					"tags^3",          // 태그
					"category_name^2"  // 카테고리
				))
				.type(TextQueryType.BestFields)
				.fuzziness("AUTO")     // 오타 허용
			)
		));
	}

	// 정렬 조건 생성
	private Sort createSort(String sortBy) {
		return switch (sortBy) {
			case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");                // 낮은 가격순
			case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");              // 높은 가격순
			case "popular" -> Sort.by(Sort.Direction.DESC, "liked_count");           // 좋아요순
			case "newest" -> Sort.by(Sort.Direction.DESC, "created_at");             // 최신순
			default -> Sort.by(Sort.Direction.DESC, "_score");                       // 기본값: _score 기준 정렬
		};
	}
}
