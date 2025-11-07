package com.domainservice.domain.product.model.entity;

import com.common.model.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Builder;
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


    @Builder
    public Product(String title, String description) {
        this.title = title;
        this.description = description;
    }

	@Override
	protected String getEntitySuffix() {
		return "";
	}
}
