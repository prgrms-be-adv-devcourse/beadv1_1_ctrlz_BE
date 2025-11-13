package com.domainservice.domain.reivew.model.entity;

import com.common.model.persistence.BaseEntity;
import com.domainservice.domain.reivew.exception.DuplicatedReviewException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "reviews")
@Getter @ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "product_post_id", nullable = false)
    private String productPostId;

    @Column(nullable = false)
    private String contents;

    @Column(name = "user_rating", nullable = false)
    private Integer userRating;     //판매자에 대한 평점

    @Column(name = "product_rating", nullable = false)
    private Integer productRating;  //판매자가 판마한 상품에 대한 평점

    @Override
    protected String getEntitySuffix() {
        return "comment";
    }

    @Builder
    public Review(
            String userId,
            String productPostId,
            String contents,
            Integer userRating,
            Integer productRating
    ) {
        this.userId = userId;
        this.productPostId = productPostId;
        this.contents = contents;
        this.userRating = userRating;
        this.productRating = productRating;
    }

    private void validateSameValue(
        String newContents,
        Integer newUserRating,
        Integer newProductRating
    ) {
        if(this.contents.equals(newContents)
            && this.userRating.equals(newUserRating)
            && this.productRating.equals(newProductRating
        )) {
            throw DuplicatedReviewException.EXCEPTION;
        }
    }

    public void updateReview(
        String contents,
        Integer userRating,
        Integer productPostRating
    ) {
        validateSameValue(contents, userRating, productPostRating);
        this.contents = contents;
        this.userRating = userRating;
        this.productRating = productPostRating;
        this.update();
    }


}

