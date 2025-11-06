package com.domainservice.domain.post.post.model.entity;

import static com.domainservice.domain.post.post.exception.vo.ProductPostExceptionCode.*;

import com.common.model.persistence.BaseEntity;
import com.domainservice.domain.post.post.exception.ProductPostException;
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

    /**
     * 상품 상태 (NEW, GOOD, FAIR)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status;

    /**
     * 거래 상태 (SELLING, PROCESSING, SOLDOUT)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "trade_status", nullable = false, length = 20)
    private TradeStatus tradeStatus;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "liked_count", nullable = false)
    private Integer likedCount;

    /**
     * BaseEntity의 추상 메서드 구현
     */
    @Override
    protected String getEntitySuffix() {
        return "product";
    }

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


    public void validateDelete(String userId) {

        // TODO: 1. 요청자가 admin 이라면 통과

        // TODO: 2. 인증된 회원의 요청인지 확인 (추후수정)
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

}