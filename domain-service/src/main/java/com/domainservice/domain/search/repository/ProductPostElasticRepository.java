package com.domainservice.domain.search.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.domainservice.domain.search.model.entity.dto.document.ProductPostDocumentEntity;

/**
 * Elasticsearch Repository
 * - 동적 쿼리는 Service -> ElasticsearchOperations 처리
 */
public interface ProductPostElasticRepository extends ElasticsearchRepository<ProductPostDocumentEntity, String> {
}