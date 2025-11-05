package com.domainservice.domain.product.tag.model.entity;

import com.domainservice.domain.product.product.model.entity.Product;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name = "Product_Posts_Tags")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductTag {

    @Id
    @Setter(AccessLevel.PRIVATE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product productPosts;

    @ManyToOne(fetch = FetchType.LAZY)
    private Tag tag;

    @Builder
    public ProductTag(Product productPosts, Tag tag) {
        this.productPosts = productPosts;
        this.tag = tag;
    }
}
