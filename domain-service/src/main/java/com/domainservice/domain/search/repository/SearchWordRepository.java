// package com.domainservice.domain.search.repository;
//
// import java.util.List;
//
// import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
//
// import com.domainservice.domain.search.model.entity.dto.document.SearchWordDocumentEntity;
//
// public interface SearchWordRepository extends ElasticsearchRepository<SearchWordDocumentEntity, String> {
//
// 	List<SearchWordDocumentEntity> findByKoreanWord(String koreanWord);
//
// 	List<SearchWordDocumentEntity> findByQwertyInput(String qwertyInput);
//
// }
