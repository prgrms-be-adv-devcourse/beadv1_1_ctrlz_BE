package com.domainservice.domain.product.product.service;

import com.domainservice.domain.product.product.model.dto.request.CreateProductPostRequest;
import com.domainservice.domain.product.product.model.dto.response.ProductPostResponse;
import com.domainservice.domain.product.product.model.entity.Product;
import com.domainservice.domain.product.product.model.enums.TradeStatus;
import com.domainservice.domain.product.product.repository.ProductRepository;
import com.domainservice.domain.product.tag.model.entity.Tag;
import com.domainservice.domain.product.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final TagRepository tagRepository;

    /**
     * 상품 게시글 생성
     */
    public ProductPostResponse createProductPost(CreateProductPostRequest request, String userId) {

        // 3. Product 생성
        Product product = Product.builder()
                .userId(userId)
                .categoryId(request.categoryId())
                .title(request.title())
                .name(request.name())
                .price(request.price())
                .description(request.description())
                .status(request.status())
                .tradeStatus(TradeStatus.SELLING)  // 기본값: 판매중
                .imageUrl(request.imageUrl())
                .build();

        List<String> tagIds = request.tagIds();
        List<Tag> findTags = tagRepository.findAllById(tagIds);
        if (findTags.size() != tagIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 태그가 포함되어있습니다.");
        }

        product.addTags(findTags);
        Product savedProduct = productRepository.save(product);

        // TODO: 판매자(user)에게 해당 게시글 정보 넣어주기

        return ProductPostResponse.from(savedProduct);
    }

}
