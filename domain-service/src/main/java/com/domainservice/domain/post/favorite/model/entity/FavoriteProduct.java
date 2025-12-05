package com.domainservice.domain.post.favorite.model.entity;

import com.common.model.persistence.BaseEntity;
import com.domainservice.domain.post.post.model.entity.ProductPost;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	uniqueConstraints = { // userId와 productPostId의 조합은 Unique, db 레벨에서 중복을 막기 위해 설정함
		@UniqueConstraint(
			name = "uk_favorite_user_product",
			columnNames = {"user_id", "product_post_id"}
		)
	}
)
public class FavoriteProduct extends BaseEntity {

	@Column(name = "user_id", nullable = false)
	private String userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_post_id", nullable = false)
	private ProductPost productPost;

	@Builder
	public FavoriteProduct(String userId, ProductPost productPost) {
		this.userId = userId;
		this.productPost = productPost;
	}

	@Override
	protected String getEntitySuffix() {
		return "favorite-product";
	}
}
