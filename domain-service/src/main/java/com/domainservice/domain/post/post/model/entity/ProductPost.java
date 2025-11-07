package com.domainservice.domain.post.post.model.entity;

import com.common.model.persistence.BaseEntity;
import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.model.dto.request.UpdateProductPostRequest;
import com.domainservice.domain.post.post.model.enums.ProductStatus;
import com.domainservice.domain.post.post.model.enums.TradeStatus;
import com.domainservice.domain.post.tag.model.entity.ProductPostTag;
import com.domainservice.domain.post.tag.model.entity.Tag;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.domainservice.domain.post.post.exception.vo.ProductPostExceptionCode.*;

/**
 * 상품 엔티티 (Product_posts 테이블)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductPost extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "categorie_id", nullable = false)
    private String categoryId;

    @OneToMany(mappedBy = "productPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductPostTag> productPostTags = new ArrayList<>();

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status; // 상품 상태 (NEW, GOOD, FAIR)

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_status", nullable = false, length = 20)
    private TradeStatus tradeStatus; // 거래 상태 (SELLING, PROCESSING, SOLDOUT)

    // TODO: s3 구현 완료 시 이미지를 여러개 업로드 할 수 있게 수정 필요
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "liked_count", nullable = false)
    private Integer likedCount;

    @Builder
    public ProductPost(String userId, String categoryId,
                       String title, String name, Integer price, String description,
                       ProductStatus status, TradeStatus tradeStatus, String imageUrl) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.name = name;
        this.price = price;
        this.description = description;
        this.status = status;
        this.tradeStatus = tradeStatus != null ? tradeStatus : TradeStatus.SELLING;
        this.imageUrl = imageUrl;
        this.viewCount = 0;
        this.likedCount = 0;
    }

    @Override
    protected String getEntitySuffix() {
        return "product";
    }

    /*
     =============== 비즈니스 로직 ===============
    */

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void update(UpdateProductPostRequest request) {
        if (request.title() != null) this.title = request.title();
        if (request.name() != null) this.name = request.name();
        if (request.price() != null) this.price = request.price();
        if (request.description() != null) this.description = request.description();
        if (request.status() != null) this.status = request.status();
        if (request.imageUrl() != null) this.imageUrl = request.imageUrl();
        this.update(); // updatedAt 최신화
    }

    public void addTags(List<Tag> tags) {
        List<ProductPostTag> productPostTags = tags.stream()
                .map(tag -> ProductPostTag
                        .builder()
                        .productPost(this)
                        .tag(tag)
                        .build())
                .toList();

        this.productPostTags.addAll(productPostTags);
    }

    // 입력 받은 태그 목록으로 교체
    public void replaceTags(List<Tag> newTags) {
        if (!this.productPostTags.isEmpty())
            this.productPostTags.clear();

        if (newTags != null && !newTags.isEmpty()) {
            addTags(newTags);
        }
    }

    /*
    ================ validate ================
     */

    public void validateDelete(String userId) {

        // TODO: 1. 요청자가 admin 이라면 통과

        // TODO: 2. 인증된 회원의 요청인지 확인 (auth 관련 추후수정)
        if (userId == null || userId.isBlank()) {
            throw new ProductPostException(UNAUTHORIZED);
        }

        // 삭제 여부 확인
        if (this.getDeleteStatus() == DeleteStatus.D) {
            throw new ProductPostException(ALREADY_DELETED);
        }

        // 거래가 진행중인 상품은 삭제 불가
        if (this.tradeStatus == TradeStatus.PROCESSING) {
            throw new ProductPostException(PRODUCT_POST_IN_PROGRESS);
        }

        // 게시글을 삭제하고자 하는 사람이 본인이 맞는지 확인
        if (!this.userId.equals(userId)) {
            throw new ProductPostException(PRODUCT_POST_FORBIDDEN);
        }

    }

    public void validateUpdate(String userId) {

        // TODO: 1. 인증된 회원의 요청인지 확인 (auth 관련 추후수정)
        if (userId == null || userId.isBlank()) {
            throw new ProductPostException(UNAUTHORIZED);
        }

        // 2. 이미 삭제 요청된 게시글이면 삭제 불가
        if (this.getDeleteStatus() == DeleteStatus.D) {
            throw new ProductPostException(ALREADY_DELETED);
        }

        // 3. 게시글 작성자 본인인지 확인
        if (!this.userId.equals(userId)) {
            throw new ProductPostException(PRODUCT_POST_FORBIDDEN);
        }

        // 4. 판매 완료된 상품은 수정 불가
        if (this.tradeStatus == TradeStatus.SOLDOUT) {
            throw new ProductPostException(CANNOT_UPDATE_SOLDOUT);
        }
    }

}