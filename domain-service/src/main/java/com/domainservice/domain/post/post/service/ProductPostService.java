package com.domainservice.domain.post.post.service;

import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.mapper.ProductPostMapper;
import com.domainservice.domain.post.post.model.dto.request.CreateProductPostRequest;
import com.domainservice.domain.post.post.model.dto.request.UpdateProductPostRequest;
import com.domainservice.domain.post.post.model.dto.response.ProductPostResponse;
import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.post.post.model.enums.TradeStatus;
import com.domainservice.domain.post.post.repository.ProductPostRepository;
import com.domainservice.domain.post.tag.model.entity.Tag;
import com.domainservice.domain.post.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.domainservice.domain.post.post.exception.vo.ProductPostExceptionCode.PRODUCT_POST_NOT_FOUND;
import static com.domainservice.domain.post.post.exception.vo.ProductPostExceptionCode.TAG_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductPostService {

    private final ProductPostRepository productPostRepository;
    private final TagRepository tagRepository;

    /**
     * 상품 게시글 생성
     */
    public ProductPostResponse createProductPost(CreateProductPostRequest request, String userId) {

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

        addTags(productPost, request.tagIds());

        ProductPost saved = productPostRepository.save(productPost);
        // TODO: 판매자(user)에게 해당 게시글 정보 넣어주기

        return ProductPostMapper.toProductPostResponse(saved);
    }

    /**
     * 상품 게시글 삭제
     */
    public String deleteProductPost(String userId, String postId) {

        // TODO: 유저가 실제로 존재하는지 정보 확인

        ProductPost target = productPostRepository.findById(postId)
                .orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));

        target.validateDelete(userId);
        target.delete(); // 상태 변경만을 진행 (SoftDelete)

        return target.getId();
    }

    public ProductPostResponse updateProductPost(String userId, String postId, UpdateProductPostRequest request) {

        ProductPost productPost = productPostRepository.findById(postId)
                .orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));

        productPost.validateUpdate(userId);

        productPost.update(request);
        addTags(productPost, request.tagIds());

        return ProductPostMapper.toProductPostResponse(productPost);
    }

    private void addTags(ProductPost productPost, List<String> tagIds) {
        if (tagIds != null) {
            List<Tag> tags = tagRepository.findAllById(tagIds);

            if (tags.size() != tagIds.size()) {
                throw new ProductPostException(TAG_NOT_FOUND);
            }
            productPost.replaceTags(tags);
        }
    }
}
