// package com.domainservice.domain.search.model.dto.response;
//
// import com.domainservice.domain.search.model.entity.dto.document.SearchWordDocumentEntity;
//
// public record SearchWordResponse(
// 	String id,
// 	String word,
// 	String qwertyInput
// ) {
// 	public static SearchWordResponse from(SearchWordDocumentEntity documentEntity) {
// 		return new SearchWordResponse(
// 			documentEntity.getId(),
// 			documentEntity.getKoreanWord(),
// 			documentEntity.getQwertyInput()
// 		);
// 	}
// }
