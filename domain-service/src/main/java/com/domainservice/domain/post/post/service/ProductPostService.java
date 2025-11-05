package com.domainservice.domain.post.post.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.exception.vo.ProductPostExceptionCode;
import com.domainservice.domain.post.post.model.dto.request.CreateProductPostRequest;
import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.post.post.model.enums.TradeStatus;
import com.domainservice.domain.post.post.repository.ProductRepository;
import com.domainservice.domain.post.tag.model.entity.Tag;
import com.domainservice.domain.post.tag.repository.TagRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductPostService {

    private final ProductRepository productRepository;
    private final TagRepository tagRepository;

    /**
     * 상품 게시글 생성
     */
    public ProductPost createProductPost(CreateProductPostRequest request, String userId) {

        ProductPost productPost = ProductPost.builder()
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
            throw new ProductPostException(ProductPostExceptionCode.TAG_NOT_FOUND);
        }

        productPost.addTags(findTags);

        // TODO: 판매자(user)에게 해당 게시글 정보 넣어주기

        return productRepository.save(productPost);
    }

}
