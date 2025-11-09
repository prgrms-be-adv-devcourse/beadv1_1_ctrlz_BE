package com.domainservice.domain.post.post.model.entity;

import com.common.model.persistence.BaseEntity;
import com.domainservice.domain.asset.image.domain.entity.Image;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 게시글-이미지 중간 테이블
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_post_images")
public class ProductPostImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_post_id", nullable = false)
    private ProductPost productPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    /**
     * 이미지 순서 (0부터 시작, 대표 이미지는 0)
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    /**
     * 대표 이미지 여부
     */
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary;

    @Builder
    public ProductPostImage(ProductPost productPost, Image image, Integer displayOrder, Boolean isPrimary) {
        this.productPost = productPost;
        this.image = image;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.isPrimary = isPrimary != null ? isPrimary : false;
    }

    @Override
    protected String getEntitySuffix() {
        return "product_post_images";
    }

    @Builder
    public ProductPostImage(ProductPost productPost, Image image) {
        this.productPost = productPost;
        this.image = image;
    }

    /**
     * 대표 이미지로 설정
     */
    public void setPrimary() {
        this.isPrimary = true;
        this.displayOrder = 0;
    }

    /**
     * 일반 이미지로 설정
     */
    public void setNotPrimary(Integer order) {
        this.isPrimary = false;
        this.displayOrder = order;
    }
}