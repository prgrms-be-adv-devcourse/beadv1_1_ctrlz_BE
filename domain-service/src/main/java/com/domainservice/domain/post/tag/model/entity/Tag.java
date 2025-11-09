package com.domainservice.domain.post.tag.model.entity;

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
public class Tag extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Override
    protected String getEntitySuffix() {
        return "tag";
    }

    @Builder
    public Tag(String name) {
        this.name = name;
    }
}