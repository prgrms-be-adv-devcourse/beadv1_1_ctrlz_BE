package com.domainservice.domain.reivew.model.dto.response;

import java.time.LocalDateTime;
import com.domainservice.common.model.user.UserResponse;
import com.domainservice.domain.order.model.dto.OrderedAt;
import com.domainservice.domain.reivew.model.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;

public record ReviewResponse(
	@Schema(description = "생성된 리뷰 ID", example = "review-uuid-9876")
	String reviewId,

	@Schema(description = "작성자 ID", example = "user-uuid-5678")
	String userId,

	@Schema(description = "작성자 닉네임", example = "구매왕")
	String nickname,

	@Schema(description = "작성자 프로필 이미지 URL", example = "https://s3.bucket/profile.jpg")
	String profileImageUrl,

	@Schema(description = "리뷰 내용", example = "배송도 빠르고 상품 상태도 설명과 같아서 좋았어요!")
	String contents,

	@Schema(description = "판매자 평점", example = "5")
	Integer userRating,

	@Schema(description = "상품 평점", example = "4")
	Integer productRating,

	@Schema(description = "주문 일시", example = "2024-03-10T15:30:00")
	LocalDateTime orderedAt,

	@Schema(description = "본인 작성 여부", example = "true")
	boolean isMine
) {
	public static ReviewResponse from(
		Review review,
		UserResponse userResponse,
		OrderedAt orderedAt,
		String userId
	) {
		return new ReviewResponse(
			review.getId(),
			review.getUserId(),
			userResponse.nickname(),
			userResponse.profileImageUrl(),
			review.getContents(),
			review.getUserRating(),
			review.getProductRating(),
			orderedAt.date(),
			userId.equals(review.getUserId())
		);
	}
}
