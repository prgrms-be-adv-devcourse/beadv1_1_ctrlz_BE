package com.domainservice.domain.product.product.model.entity;

import com.common.model.persistence.BaseEntity;
import com.domainservice.domain.product.product.model.enums.ProductStatus;
import com.domainservice.domain.product.product.model.enums.TradeStatus;
import com.domainservice.domain.product.tag.model.entity.ProductTag;
import com.domainservice.domain.product.tag.model.entity.Tag;
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
@Table(name = "Product_posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "categorie_id", nullable = false)
    private String categoryId;

    @OneToMany(mappedBy = "productPosts", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductTag> productTags = new ArrayList<>();

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
    public Product(String userId, String categoryId,
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
        List<ProductTag> productTags = tags.stream()
                .map(tag -> ProductTag
                        .builder()
                        .productPosts(this)
                        .tag(tag)
                        .build())
                .toList();

        this.productTags.addAll(productTags);
    }

}