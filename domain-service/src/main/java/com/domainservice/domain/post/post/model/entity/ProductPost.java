package com.domainservice.domain.post.post.model.entity;

import com.common.model.persistence.BaseEntity;
import com.domainservice.domain.asset.image.domain.entity.Image;
import com.domainservice.domain.post.post.exception.ProductPostException;
import com.domainservice.domain.post.post.model.dto.request.ProductPostRequest;
import com.domainservice.domain.post.post.model.enums.ProductStatus;
import com.domainservice.domain.post.post.model.enums.TradeStatus;
import com.domainservice.domain.post.tag.model.entity.ProductPostTag;
import com.domainservice.domain.post.tag.model.entity.Tag;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.common.exception.vo.ProductPostExceptionCode.*;

/**
 * 상품 엔티티 (Product_posts 테이블)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductPost extends BaseEntity {

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Column(name = "categorie_id", nullable = false)
	private String categoryId;

	@OneToMany(mappedBy = "productPost", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProductPostTag> productPostTags = new ArrayList<>();

	@OneToMany(mappedBy = "productPost", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProductPostImage> productPostImages = new ArrayList<>();

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "price", nullable = false)
	private Integer price;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private ProductStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "trade_status", nullable = false, length = 20)
	private TradeStatus tradeStatus;

	@Column(name = "view_count", nullable = false)
	private Integer viewCount;

	@Column(name = "liked_count", nullable = false)
	private Integer likedCount;

	@Builder
	public ProductPost(String userId, String categoryId,
		String title, String name, Integer price, String description,
		ProductStatus status, TradeStatus tradeStatus) {
		this.userId = userId;
		this.categoryId = categoryId;
		this.title = title;
		this.name = name;
		this.price = price;
		this.description = description;
		this.status = status;
		this.tradeStatus = tradeStatus != null ? tradeStatus : TradeStatus.SELLING;
		this.viewCount = 0;
		this.likedCount = 0;
	}

	@Override
	protected String getEntitySuffix() {
		return "product";
	}

    /*
     =============== 비즈니스 로직 ===============
    */

	public void incrementViewCount() {
		this.viewCount++;
	}

    public void update(ProductPostRequest request) {
        this.title = request.title();
        this.name = request.name();
        this.price = request.price();
        this.description = request.description();
        this.status = request.status();
        this.update(); // updatedAt 최신화
    }

	/**
	 * 상품 거래 상태 변경 메서드들
	 */
	public void markAsProcessing() {
		this.tradeStatus = TradeStatus.PROCESSING;
		this.updateTime();
	}

	public void markAsSoldout() {
		this.tradeStatus = TradeStatus.SOLDOUT;
		this.updateTime();
	}

	public void markAsSellingAgain() {
		this.tradeStatus = TradeStatus.SELLING;
		this.updateTime();
	}

    /*
     =============== 태그 ===============
    */

	public void addTags(List<Tag> tags) {
		List<ProductPostTag> productPostTags = tags.stream()
			.map(tag -> ProductPostTag
				.builder()
				.productPost(this)
				.tag(tag)
				.build())
			.toList();

		this.productPostTags.addAll(productPostTags);
	}

	public void replaceTags(List<Tag> newTags) {
		if (!this.productPostTags.isEmpty())
			this.productPostTags.clear();

        if (newTags != null) {
            addTags(newTags);
        }
    }

    /*
     =============== 이미지 ===============
    */

	/**
	 * 이미지 추가 (여러 개)
	 * 첫 번째 이미지가 대표 이미지
	 */
	public void addImages(List<Image> images) {
		if (images == null || images.isEmpty()) {
			return;
		}

		for (int i = 0; i < images.size(); i++) {
			ProductPostImage productPostImage = ProductPostImage.builder()
				.productPost(this)
				.image(images.get(i))
				.displayOrder(i)
				.isPrimary(i == 0)  // 첫 번째가 대표 이미지
				.build();

			this.productPostImages.add(productPostImage);
		}
	}

	/**
	 * 대표 이미지 URL 조회
	 */
	public String getPrimaryImageUrl() {
		return this.productPostImages.stream()
			.filter(ProductPostImage::getIsPrimary)
			.findFirst()
			.map(productPostImage -> productPostImage.getImage().getS3Url())
			.orElse(null);
	}

	/**
	 * 모든 이미지 URL 조회
	 */
	public List<String> getAllImageUrls() {
		return this.productPostImages.stream()
			.sorted(Comparator.comparing(ProductPostImage::getDisplayOrder))
			.map(ppi -> ppi.getImage().getS3Url())
			.toList();
	}

    /*
    ================ validate ================
     */

    public void validateUpdate(String userId, List<String> roles) {

        // 관리자는 검증 없이 게시글 수정 가능
        if (roles.contains("ADMIN")) {
            return;
        }

        // 판매 완료된 상품은 수정 불가
        if (this.tradeStatus == TradeStatus.SOLDOUT) {
            throw new ProductPostException(CANNOT_UPDATE_SOLDOUT);
        }
        validate(userId);
    }

    public void validateDelete(String userId, List<String> roles) {

        // 관리자는 검증 없이 게시글 삭제 가능
        if (roles.contains("ADMIN")) {
            return;
        }

        validate(userId);
    }

    public void validate(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ProductPostException(UNAUTHORIZED);
        }

        // soft delete 된 상품은 수정 혹은 삭제 불가
        if (this.getDeleteStatus() == DeleteStatus.D) {
            throw new ProductPostException(ALREADY_DELETED);
        }

        // 거래가 진행중인 상품은 수정 혹은 삭제 불가
        if (this.tradeStatus == TradeStatus.PROCESSING) {
            throw new ProductPostException(PRODUCT_POST_IN_PROGRESS);
        }

        // 본인이 작성한 글만 수정 혹은 삭제 가능
        if (!this.userId.equals(userId)) {
            throw new ProductPostException(PRODUCT_POST_FORBIDDEN);
        }
    }
}