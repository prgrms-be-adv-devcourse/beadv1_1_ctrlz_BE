package com.domainservice.domain.search.repository.impl;

import java.util.List;

import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.stereotype.Repository;

import com.domainservice.domain.search.model.entity.dto.document.SearchWordDocumentEntity;
import com.domainservice.domain.search.repository.SearchWordQueryRepository;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FuzzyQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.PrefixQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SearchWordQueryRepositoryImpl implements SearchWordQueryRepository {

	private final ElasticsearchOperations operations;

	@Override
	public List<SearchWordDocumentEntity> findAllByQwertyInput(String prefix) {

		Query fuzzyPrefixQuery = BoolQuery.of(b -> b
			.should(
				PrefixQuery.of(p -> p
					.field("qwertyInput")
					.value(prefix)
				)._toQuery()
			)
			.should(
				FuzzyQuery.of(f -> f
					.field("qwertyInput")
					.value(prefix)
					.fuzziness("AUTO")
					.prefixLength(Math.min(prefix.length(), 2))  // 🔧 오타 허용 범위 개선
				)._toQuery()
			)
		)._toQuery();

		return executeSearch(fuzzyPrefixQuery);
	}

	@Override
	public List<SearchWordDocumentEntity> findAllByOriginValue(String koreanWord) {

		Query fuzzyPrefixQuery = BoolQuery.of(b -> b
			.should(
				PrefixQuery.of(p -> p
					.field("originValue")
					.value(koreanWord)
				)._toQuery()
			)
			.should(
				FuzzyQuery.of(f -> f
					.field("originValue")
					.value(koreanWord)
					.fuzziness("AUTO")
					.prefixLength(Math.min(koreanWord.length(), 2))
				)._toQuery()
			)
		)._toQuery();

		return executeSearch(fuzzyPrefixQuery);
	}

	/**
	 * @description originValue 기준으로 upsert 처리
	 * Document에 originValue가 있으면 update 없으면 insert
	 * @param searchWordDocumentEntities
	 */
	@Override
	public void upsertByOriginValue(List<SearchWordDocumentEntity> searchWordDocumentEntities) {

		List<IndexQuery> indexQueries = searchWordDocumentEntities.stream()
			.map(entity ->
				IndexQuery.builder()
					.withId(entity.getOriginValue())   // originValue를 문서 ID로 사용
					.withObject(entity)                // 전체 document 저장
					.build()
			)
			.toList();

		// indexName = "search-words"
		operations.bulkIndex(
			indexQueries,
			IndexCoordinates.of("search-words")
		);
	}

	private List<SearchWordDocumentEntity> executeSearch(Query query) {
		NativeQuery searchQuery = NativeQuery.builder()
			.withQuery(query)
			.withMaxResults(10)
			.build();

		SearchHits<SearchWordDocumentEntity> searchHits = operations.search(
			searchQuery,
			SearchWordDocumentEntity.class
		);

		return searchHits.getSearchHits().stream()
			.map(SearchHit::getContent)
			.toList();
	}
}
