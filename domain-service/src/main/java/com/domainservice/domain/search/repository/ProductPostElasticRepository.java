package com.domainservice.domain.search.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.domainservice.domain.search.model.entity.dto.document.ProductPostDocumentEntity;

public interface ProductPostElasticRepository extends ElasticsearchRepository<ProductPostDocumentEntity, String> {

	@Query("""
		{
			"multi_match": {
			  "query": "#{#query}",
			  "fields": [
				"title^3",
				"description^2",
				"tags"
			  ],
			  "type": "best_fields"
			}
		  }
		""")
	SearchPage<ProductPostDocumentEntity> search(String query, Pageable pageable);

}