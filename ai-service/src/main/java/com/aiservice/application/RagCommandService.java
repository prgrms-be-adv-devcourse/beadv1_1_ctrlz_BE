package com.aiservice.application;

import org.springframework.stereotype.Service;

import com.aiservice.application.command.CreateProductVectorCommand;
import com.aiservice.domain.model.ProductVectorContent;
import com.aiservice.domain.repository.VectorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class RagCommandService implements RagService<CreateProductVectorCommand> {

	private final VectorRepository vectorRepository;

	@Override
	public String uploadData(CreateProductVectorCommand data) {
		
		vectorRepository.findDocumentByProductId(data.productId())
				.ifPresent(doc -> {
					log.info("기존 상품 벡터 삭제: {}", data.productId());
					vectorRepository.deleteDocument(data.productId());
				});

		ProductVectorContent content = ProductVectorContent.builder()
				.productId(data.productId())
				.title(data.title())
				.name(data.name())
				.categoryName(data.categoryName())
				.status(data.status())
				.price(data.price())
				.description(data.description())
				.tags(data.tags())
				.build();

		return vectorRepository.addDocument(content);
	}

	@Override
	public void deleteData(String productId) {
		vectorRepository.deleteDocument(productId);
	}
}
