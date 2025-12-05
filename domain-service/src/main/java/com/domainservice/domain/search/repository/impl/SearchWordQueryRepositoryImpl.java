package com.domainservice.domain.search.repository.impl;

import java.util.List;

import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
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
	public List<SearchWordDocumentEntity> findAllByKoreanWord(String koreanWord) {

		Query fuzzyPrefixQuery = BoolQuery.of(b -> b
			.should(
				PrefixQuery.of(p -> p
					.field("koreanWord")
					.value(koreanWord)
				)._toQuery()
			)
			.should(
				FuzzyQuery.of(f -> f
					.field("koreanWord")
					.value(koreanWord)
					.fuzziness("AUTO")
					.prefixLength(Math.min(koreanWord.length(), 2))
				)._toQuery()
			)
		)._toQuery();

		return executeSearch(fuzzyPrefixQuery);
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
