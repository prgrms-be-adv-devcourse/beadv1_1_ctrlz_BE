package com.domainservice.domain.post.post.service;

import com.common.model.persistence.BaseEntity;
import com.common.model.web.PageResponse;
import com.domainservice.domain.asset.image.application.ImageService;
import com.domainservice.domain.asset.image.domain.entity.Image;
import com.domainservice.domain.asset.image.domain.entity.ImageTarget;
import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.mapper.ProductPostMapper;
import com.domainservice.domain.post.post.model.dto.request.CreateProductPostRequest;
import com.domainservice.domain.post.post.model.dto.request.UpdateProductPostRequest;
import com.domainservice.domain.post.post.model.dto.response.ProductPostResponse;
import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.post.post.model.entity.ProductPostImage;
import com.domainservice.domain.post.post.model.enums.ProductStatus;
import com.domainservice.domain.post.post.model.enums.TradeStatus;
import com.domainservice.domain.post.post.repository.ProductPostRepository;
import com.domainservice.domain.post.tag.model.entity.Tag;
import com.domainservice.domain.post.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.domainservice.domain.post.post.exception.vo.ProductPostExceptionCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductPostService {

    private final ProductPostRepository productPostRepository;
    private final TagRepository tagRepository;
    private final ImageService imageService;

    /**
     * 상품 게시글 생성 (이미지 포함)
     */
    public ProductPostResponse createProductPost(
            CreateProductPostRequest request, String userId, List<MultipartFile> images) {

        ProductPost productPost = ProductPost.builder()
                .userId(userId)
                .categoryId(request.categoryId())
                .title(request.title())
                .name(request.name())
                .price(request.price())
                .description(request.description())
                .status(request.status())
                .tradeStatus(TradeStatus.SELLING)
                .build();

        // 첨부된 이미지가 존재하면 imageService를 통해 s3 업로드 후 productPost에 추가
        if (images != null && !images.isEmpty()) {
            if (images.size() > 10) {
                throw new ProductPostException(TOO_MANY_IMAGES);
            }
            List<Image> uploadedImages = imageService.uploadProfileImageListByTarget(images, ImageTarget.PRODUCT);
            productPost.addImages(uploadedImages);
        }

        addTags(productPost, request.tagIds());

        ProductPost saved = productPostRepository.save(productPost);

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

        List<ProductPostImage> imagesToDelete = target.getProductPostImages();

        // 저장된 각 이미지를 s3에서 삭제
        imagesToDelete.stream()
                .map(ProductPostImage::getImage)
                .forEach(image -> imageService.deleteProfileImageById(image.getId()));

        // clear 하게되면 'orphanRemoval = true' 옵션에 의해 ProductPostImage가 DB에서 자동으로 삭제됨
        target.getProductPostImages().clear();

        // soft delete 처리
        target.delete();

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

    public ProductPostResponse getProductPostById(String postId) {

        ProductPost productPost = productPostRepository.findById(postId)
                .orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));

        // Soft Delete로 삭제된 상품은 상세 조회 불가
        if (productPost.getDeleteStatus() == BaseEntity.DeleteStatus.D) {
            throw new ProductPostException(PRODUCT_POST_DELETED);
        }

        productPost.incrementViewCount();

        return ProductPostMapper.toProductPostResponse(productPost);
    }

    /**
     * 상품 게시글 목록 조회 (페이징 + 동적 필터링)
     */
    public PageResponse<List<ProductPostResponse>> getProductPostList(
            Pageable pageable, String categoryId, ProductStatus status,
            TradeStatus tradeStatus, Integer minPrice, Integer maxPrice
    ) {

        // Specification 생성
        Specification<ProductPost> spec = ProductPostSpecification.searchWith(
                categoryId, status, tradeStatus, minPrice, maxPrice
        );

        Page<ProductPost> page = productPostRepository.findAll(spec, pageable);

        // PageResponse 생성
        return new PageResponse<>(
                page.getNumber(),
                page.getTotalPages(),
                page.getSize(),
                page.hasNext(),
                page.getContent().stream()
                        .map(ProductPostMapper::toProductPostResponse)
                        .toList()
        );
    }

    // TODO: 상품 판매상태 변경
    // TODO: 내가 구매한 상품 조회
    // TODO: 내가 판매한 상품 조회
    // TODO: 좋아요 로직 구현
    // TODO: 찜한 게시물
    // TODO: 최근 본 상품 redis?
}
