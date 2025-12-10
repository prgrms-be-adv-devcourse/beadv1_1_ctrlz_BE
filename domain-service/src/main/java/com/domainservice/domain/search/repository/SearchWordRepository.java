package com.domainservice.domain.search.repository;

import java.util.Optional;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.domainservice.domain.search.model.entity.dto.document.SearchWordDocumentEntity;

public interface SearchWordRepository extends
	ElasticsearchRepository<SearchWordDocumentEntity, String>, SearchWordQueryRepository {

	Optional<SearchWordDocumentEntity> findByOriginValue(String koreanWord);
	Optional<SearchWordDocumentEntity> findByQwertyInput(String qwertyInput);

}
