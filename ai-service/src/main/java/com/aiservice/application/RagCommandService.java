package com.aiservice.application;

import org.springframework.stereotype.Service;

import com.aiservice.domain.model.ProductVectorContent;
import com.aiservice.domain.repository.VectorRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RagCommandService implements RagService<ProductVectorContent> {

	private final VectorRepository vectorRepository;

	/*
	* TODO: 상품 정보 데이터를 저장한다.
	*/
	@Override
	public String uploadData(ProductVectorContent data) {
		return vectorRepository.addDocument(data);
	}
}
