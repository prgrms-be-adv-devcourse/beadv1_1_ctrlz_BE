package com.domainservice.domain.post.post.service;

import com.common.model.persistence.BaseEntity;
import com.common.model.web.PageResponse;
import com.domainservice.domain.asset.image.application.ImageService;
import com.domainservice.domain.asset.image.domain.entity.Image;
import com.domainservice.domain.asset.image.domain.entity.ImageTarget;
import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.mapper.ProductPostMapper;
import com.domainservice.domain.post.post.model.dto.request.ProductPostRequest;
import com.domainservice.domain.post.post.model.dto.response.ProductPostResponse;
import com.domainservice.domain.post.post.model.entity.ProductPost;
import com.domainservice.domain.post.post.model.enums.ProductStatus;
import com.domainservice.domain.post.post.model.enums.TradeStatus;
import com.domainservice.domain.post.post.repository.ProductPostRepository;
import com.domainservice.domain.post.tag.model.entity.Tag;
import com.domainservice.domain.post.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.domainservice.domain.post.post.exception.vo.ProductPostExceptionCode.*;

@Slf4j
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
            ProductPostRequest request, String userId, List<MultipartFile> imageFiles) {

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

        addTags(productPost, request.tagIds());

        // 첨부된 이미지를 s3 업로드 후 productPost에 추가
        validateUploadImage(imageFiles);
        uploadAndAddImages(productPost, imageFiles);

        ProductPost saved = productPostRepository.save(productPost);

        return ProductPostMapper.toProductPostResponse(saved);
    }

    /**
     * 상품 게시글 수정
     */
    public ProductPostResponse updateProductPost
    (ProductPostRequest request, List<MultipartFile> imageFiles, String userId, String postId) {

        ProductPost productPost = productPostRepository.findById(postId)
                .orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));

        // 게시물이 수정 가능한 상태인지 유효성 검사
        productPost.validate(userId);

        // 기존 저장된 이미지 삭제하고 새 이미지로 교체
        replaceImages(productPost, imageFiles);

        // 게시글에 입력받은 tag 추가
        addTags(productPost, request.tagIds());

        // request 요청으로 게시글 정보 변경
        productPost.update(request);

        return ProductPostMapper.toProductPostResponse(productPost);
    }

    /**
     * 상품 게시글 삭제
     */
    public String deleteProductPost(String userId, String postId) {

        // TODO: 유저가 실제로 존재하는지 정보 확인

        ProductPost target = productPostRepository.findById(postId)
                .orElseThrow(() -> new ProductPostException(PRODUCT_POST_NOT_FOUND));

        // 게시물이 삭제 가능한 상태인지 유효성 검사
        target.validate(userId);

        // 테이블에 저장된 이미지 삭제
        deleteProductPostImages(target);

        // soft delete 처리
        target.delete();

        return target.getId();
    }

    /**
     * 단일 상품 게시글 조회
     */
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

    /*
    ================= private Method =================
     */

    /**
     * 기존 이미지를 모두 삭제하고 새로운 이미지로 교체합니다.
     */
    private void replaceImages(ProductPost productPost, List<MultipartFile> imageFiles) {

        // 첨부한 이미지 유효성 체크
        validateUploadImage(imageFiles);

        // 테이블에서 기존 이미지 삭제
        deleteProductPostImages(productPost);

        // 새 이미지 업로드 및 productPost에 추가
        uploadAndAddImages(productPost, imageFiles);

    }

    private void uploadAndAddImages(ProductPost productPost, List<MultipartFile> imageFiles) {
        List<Image> uploadedImages = imageService.uploadProfileImageListByTarget(imageFiles, ImageTarget.PRODUCT);
        productPost.addImages(uploadedImages);
    }

    private void validateUploadImage(List<MultipartFile> imageFiles) {
        // 게시글 등록 시 이미지 반드시 1개는 필요, 없으면 예외처리
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new ProductPostException(IMAGE_REQUIRED);
        }

        // 10개를 초과해서 등록하더라도 예외처리
        if (imageFiles.size() > 10) {
            throw new ProductPostException(TOO_MANY_IMAGES);
        }
    }

    // 저장된 각 이미지를 s3에서 삭제
    private void deleteProductPostImages(ProductPost target) {
        List<String> targetIds = target.getProductPostImages().stream()
                .map(e -> e.getImage().getId())
                .toList();

        // clear 하게되면 'orphanRemoval = true' 옵션에 의해 ProductPostImage를 DB에 DELETE 요청함
        target.getProductPostImages().clear();
        productPostRepository.flush();

        targetIds.forEach(imageService::deleteProfileImageById);
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

    // TODO: 상품 판매상태 변경
    // TODO: 내가 구매한 상품 조회
    // TODO: 내가 판매한 상품 조회
    // TODO: 좋아요 로직 구현
    // TODO: 찜한 게시물
    // TODO: 최근 본 상품 redis?
}
