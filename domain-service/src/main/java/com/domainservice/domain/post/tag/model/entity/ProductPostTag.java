package com.domainservice.domain.post.tag.model.entity;

import com.domainservice.domain.post.post.model.entity.ProductPost;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductPostTag {

    @Id
    @Setter(AccessLevel.PRIVATE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductPost productPost;

    @ManyToOne(fetch = FetchType.LAZY)
    private Tag tag;

    @Builder
    public ProductPostTag(ProductPost productPost, Tag tag) {
        this.productPost = productPost;
        this.tag = tag;
    }
}
