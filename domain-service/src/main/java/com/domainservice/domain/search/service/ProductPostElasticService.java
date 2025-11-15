package com.domainservice.domain.search.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import com.common.model.web.PageResponse;
import com.domainservice.domain.search.mapper.SearchMapper;
import com.domainservice.domain.search.model.entity.dto.document.ProductPostDocumentEntity;
import com.domainservice.domain.search.model.entity.dto.request.ProductPostSearchRequest;
import com.domainservice.domain.search.model.entity.dto.response.ProductPostSearchResponse;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPostElasticService {

	private final ElasticsearchOperations elasticsearchOperations;

	/**
	 * 통합 검색 API
	 *
	 * @param request  검색 요청 (q, category, minPrice, maxPrice, tags, sort)
	 * @param pageable 페이징 정보
	 * @return 검색 결과
	 */
	public PageResponse<List<ProductPostSearchResponse>> search(
		ProductPostSearchRequest request, Pageable pageable) {

		// NativeQuery 생성
		NativeQuery searchQuery = buildNativeQuery(request, pageable);

		// Elasticsearch에 종합된 쿼리로 검색 실행
		SearchHits<ProductPostDocumentEntity> searchHits =
			elasticsearchOperations.search(searchQuery, ProductPostDocumentEntity.class);

		long totalHits = searchHits.getTotalHits();
		int totalPages = (int)Math.ceil((double)totalHits / pageable.getPageSize());

		log.info("📊 검색 결과 - Total hits: {}, Page: {}/{}",
			totalHits, pageable.getPageNumber(), totalPages);

		return new PageResponse<>(
			pageable.getPageNumber(),
			totalPages,
			pageable.getPageSize(),
			pageable.getPageNumber() < totalPages - 1,
			SearchMapper.toProductPostSearchResponses(searchHits) // 결과값을 responses로 매핑
		);
	}

	private NativeQuery buildNativeQuery(ProductPostSearchRequest request, Pageable pageable) {

		/*
		1. Bool Query 생성
		bool{} : 여러 쿼리들을 AND/OR처럼 묶어서 한 번에 평가하는 상위 컨테이너
		 */
		Query boolQuery = buildBoolQuery(request);

		// 정렬 적용
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

		// must 쿼리 리스트

		// must: 검색 점수에도 반영되는 “필수 조건” 영역, 내부 필드가 반드시 만족해야 문서가 검색됨
		List<Query> mustQueries = new ArrayList<>();

		// multi_match: 하나의 검색어(?0)를 여러 필드(name, title, description 등)에 동시에 매칭시키는 검색 쿼리
		if (request.hasQuery()) {
			mustQueries.add(buildMultiMatchQuery(request.q()));
		}

		// 검색어 없으면 match_all
		if (mustQueries.isEmpty()) {
			mustQueries.add(
				Query.of(queryBuilder -> queryBuilder.matchAll(
					matchAllBuilder -> matchAllBuilder
				))
			);
		}

		// 2. filter: 입력된 필수 조건들 적용
		List<Query> filterQueries = new ArrayList<>();

		// 2-1. 삭제되지 않은 상품만 필터
		// terms : “주어진 값 리스트 중 하나라도 일치하는 문서만 남겨라” 라는 필터/쿼리
		filterQueries.add(
			Query.of(queryBuilder -> queryBuilder.term(
				TermQuery.of(termQueryBuilder -> termQueryBuilder
					.field("delete_status")
					.value("N")
				)
			))
		);

		// 2-2. 카테고리
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

		// 2-3. 가격 범위
		// gte: greater than or equal, 이 값 이상(≥)
		// lte: less than or equal, 이 값 이하(≤)
		if (request.hasPriceFilter()) {
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
		}

		// 2-4. 태그
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

		// 적용된 must와 filter를 Bool Query로 반환
		return Query.of(queryBuilder -> queryBuilder.bool(
			BoolQuery.of(boolQueryBuilder -> boolQueryBuilder
				.must(mustQueries)
				.filter(filterQueries)
			)
		));
	}

	// Multi-Match 쿼리 생성 (검색어)
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
			case "listing_count_desc" -> Sort.by(Sort.Direction.DESC, "view_count"); // TODO: 인기순 기준(view_count 외) 적용 필요
			default -> Sort.by(Sort.Direction.DESC, "_score");                       // 기본값: _score 기준 정렬
		};
	}
}
