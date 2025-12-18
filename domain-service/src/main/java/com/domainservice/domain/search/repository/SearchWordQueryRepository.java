package com.domainservice.domain.search.repository;

import java.util.List;

import com.domainservice.domain.search.model.entity.dto.document.SearchWordDocumentEntity;

public interface SearchWordQueryRepository {

	 List<SearchWordDocumentEntity> findAllByQwertyInput(String qwertyInput);

	 List<SearchWordDocumentEntity> findAllByOriginValue(String koreanWord);

	 void upsertByOriginValue(List<SearchWordDocumentEntity> searchWordDocumentEntities);
}
