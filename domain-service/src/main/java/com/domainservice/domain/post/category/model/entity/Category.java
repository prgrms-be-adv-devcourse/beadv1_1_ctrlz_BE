package com.domainservice.domain.post.category.model.entity;

import com.common.model.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Override
    protected String getEntitySuffix() {
        return "category";
    }

    @Builder
    public Category(String name) {
        this.name = name;
    }
}
