package com.domainservice.domain.search.service;

import static com.common.exception.vo.ProductPostExceptionCode.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import com.common.model.web.PageResponse;
import com.domainservice.domain.search.exception.ElasticSearchException;
import com.domainservice.domain.search.mapper.SearchMapper;
import com.domainservice.domain.search.model.entity.dto.document.ProductPostDocumentEntity;
import com.domainservice.domain.search.model.entity.dto.response.ProductPostSearchResponse;
import com.domainservice.domain.search.repository.ProductPostElasticRepository;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MoreLikeThisQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPostRecommendationService {

	private final ElasticsearchOperations elasticsearchOperations;
	private final ProductPostElasticRepository productPostElasticRepository;

	/**
	 * 비슷한 상품 추천
	 * - More Like This + Multi-Match 조합으로 정확한 유사도 검색
	 */
	public PageResponse<List<ProductPostSearchResponse>> findSimilarProducts(
		String postId, Pageable pageable) {

		// 기준 상품 조회
		ProductPostDocumentEntity baseProduct = productPostElasticRepository.findById(postId)
			.orElseThrow(() -> new ElasticSearchException(PRODUCT_POST_NOT_FOUND));

		// More Like This + Multi-Match 쿼리 생성
		Query similarQuery = buildSimilarProductQuery(baseProduct);

		// NativeQuery 생성
		NativeQuery searchQuery = NativeQuery.builder()
			.withQuery(similarQuery)
			.withPageable(pageable)
			.build();

		// 검색 실행
		SearchHits<ProductPostDocumentEntity> searchHits = elasticsearchOperations
			.search(searchQuery, ProductPostDocumentEntity.class);

		long totalHits = searchHits.getTotalHits();
		int totalPages = (int)Math.ceil((double)totalHits / pageable.getPageSize());

		return new PageResponse<>(
			pageable.getPageNumber(),
			totalPages,
			pageable.getPageSize(),
			pageable.getPageNumber() < totalPages - 1,
			SearchMapper.toSearchResponseList(searchHits)
		);
	}

	public PageResponse<List<ProductPostSearchResponse>> findSellerProducts(
		Long productPostId, Pageable pageable) {
		return null;
	}

	public PageResponse<List<ProductPostSearchResponse>> findPopularInCategory(
		Long productPostId, Pageable pageable) {
		return null;
	}

	/*
	================= Private Methods =================
	 */

	/**
	 * 유사 상품 검색 쿼리 생성
	 * - More Like This (기본 유사도) + Should (정확도 보정)
	 */
	private Query buildSimilarProductQuery(ProductPostDocumentEntity baseProduct) {

		// More Like This 쿼리 (유사도 기반 검색)
		Query mltQuery = buildMoreLikeThisQuery(baseProduct);

		// Should 절 (정확도 보정)
		List<Query> shouldQueries = buildShouldQueries(baseProduct);

		// Bool Query로 조합
		return Query.of(q -> q.bool(
			BoolQuery.of(bool -> bool
				.must(mltQuery)
				.should(shouldQueries)
				.minimumShouldMatch("1")  // Should 중 최소 1개는 매칭
				.filter(List.of(
					// 현재 상품 제외
					Query.of(filter -> filter.bool(
						b -> b.mustNot(
							Query.of(mustNot -> mustNot.term(
								term -> term.field("_id").value(baseProduct.getId())
							))
						)
					)),
					// 삭제되지 않은 상품만
					Query.of(filter -> filter.term(
						term -> term.field("delete_status").value("N")
					))
				))
			)
		));
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
				// 키워드 추출 ( 어떤 단어를 기준으로 유사도를 검사할건지? )
				.minTermFreq(1) 		 	    // 기준 문서 (like) 안에서 최소 1번만 등장해도 키워드 후보로 추출
				.minDocFreq(1) 				// es의 전체 DB에서 1번만 등장해도 유효한 키워드로 인정
				.maxQueryTerms(25)           // 추출된 후보 중 점수가 높은 상위 25개만 실제 검색 쿼리로 사용
				.minimumShouldMatch("20%")   // 사용된 25개 키워드 중 20% 이상 일치하는 문서만 결과로 반환
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