package com.domainservice.domain.search.service.search.query;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Component;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

@Component
public class DailyProductQueryBuilder {

	/**
	 * 오늘의 추천 상품 조회 쿼리 생성
	 * - FunctionScore를 사용하여 조회수와 좋아요 수를 기준으로 가중치 부여
	 * - 부여된 가중치를 기준으로 계산된 점수로 내림차순하여 상품을 조회
	 *
	 * 점수 계산 공식:
	 * - 좋아요 점수: sqrt(liked_count) * 5.0
	 * - 조회수 점수: log(view_count + 1) * 3.0
	 * - 최종 점수: 좋아요 점수 + 조회수 점수
	 * (좋아요에 더 높은 가중치를 부여했음)
	 */
	public NativeQuery build(String category, Pageable pageable) {

		// 필터 조건 리스트
		List<Query> filterQueries = new ArrayList<>();

		filterQueries.add(Query.of(f -> f.term(t -> t.field("delete_status").value("N"))));
		filterQueries.add(Query.of(f -> f.term(t -> t.field("trade_status").value("SELLING"))));

		// 최근 72시간 내 등록된 상품
		filterQueries.add(Query.of(f -> f
			.range(r -> r.date(d -> d.field("created_at").gte("now-72h")))
		));

		// 카테고리 필터 (선택)
		if (!category.equals("ALL")) {
			filterQueries.add(Query.of(f -> f.term(t -> t.field("category_name.keyword").value(category))));
		}

		// Bool Query 생성
		Query boolQuery = Query.of(q -> q.bool(
			BoolQuery.of(bool -> bool.filter(filterQueries))
		));

		// Function Score 함수 정의
		List<FunctionScore> functions = List.of(
			// 가중치 1 : 좋아요 수
			FunctionScore.of(fs -> fs
				.fieldValueFactor(fvf -> fvf
					.field("liked_count")
					.factor(5.0)                            // 좋아요 1개당 5점
					.modifier(FieldValueFactorModifier.Sqrt)        // 좋아요 수에 제곱근 적용
					.missing(0.0)                           // 좋아요가 없으면 0점
				)
			),
			// 가중치 2 : 조회수
			FunctionScore.of(fs -> fs
				.fieldValueFactor(fvf -> fvf
					.field("view_count")
					.factor(3.0)                            // 조회수 1회당 3점
					.modifier(FieldValueFactorModifier.Log1p)       // log(1 + x) 적용
					.missing(0.0)                           // 값이 없으면 0
				)
			)
		);

		// Function Score Query 생성
		Query functionScoreQuery = Query.of(q -> q.functionScore(
			FunctionScoreQuery.of(fs -> fs
				.query(boolQuery)
				.functions(functions)
				.scoreMode(FunctionScoreMode.Sum)              // Function Score 함수들의 점수를 합산
				.boostMode(FunctionBoostMode.Replace)          // filter 쿼리 점수는 무시, function 점수만 사용
			)
		));

		// NativeQuery로 변환하여 반환
		return NativeQuery.builder()
			.withQuery(functionScoreQuery)
			.withPageable(pageable)
			.build();
	}
}