package com.domainservice.domain.product.model.entity;

import com.common.model.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /*
    ...
     */

    @Override
    protected String getEntityPrefix() {
        return "product"; // 해당 메서드를 통해 "product-UUID" 형태의 code가 자동 생성됨
    }

}
