package com.domainservice.domain.post.post.model.dto.response;

import static com.common.model.persistence.BaseEntity.*;

import java.time.LocalDateTime;
import java.util.List;

import com.common.model.vo.ProductStatus;
import com.common.model.vo.TradeStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ProductPostDescription(
	@Schema(description = "게시글 ID", example = "post-uuid-1234")
	String id,

	@Schema(description = "판매자 닉네임", example = "쿨거래장인")
	String nickname,

	@Schema(description = "상품명", example = "아이폰 15 Pro 256GB")
	String name,

	@Schema(description = "게시글 제목", example = "아이폰 15 Pro 급처합니다")
	String title,

	@Schema(description = "카테고리 이름", example = "전자기기")
	String categoryName,

	@Schema(description = "가격", example = "1200000")
	Integer price,

	@Schema(description = "상품 설명", example = "구매한지 1달 된 새상품급입니다. 박스 포함 풀구성입니다.")
	String description,

	@Schema(description = "태그 목록", example = "[\"애플\", \"아이폰\", \"스마트폰\"]")
	List<String> tags,

	@Schema(description = "상품 이미지 URL 목록", example = "[\"https://s3.bucket/img1.jpg\", \"https://s3.bucket/img2.jpg\"]")
	List<String> imageUrls,

	@Schema(description = "대표 이미지 URL", example = "https://s3.bucket/img1.jpg")
	String primaryImageUrl,

	@Schema(description = "조회수", example = "150")
	Integer viewCount,

	@Schema(description = "관심(찜) 수", example = "12")
	Integer likedCount,

	@Schema(description = "상품 상태 (NEW, GOOD, FAIR)", example = "NEW")
	ProductStatus status,

	@Schema(description = "거래 상태 (SELLING, PROCESSING, SOLDOUT)", example = "SELLING")
	TradeStatus tradeStatus,

	@Schema(description = "삭제 상태 ( N(not), D(deleted) )", example = "N")
	DeleteStatus deleteStatus,

	@Schema(description = "생성일시", example = "2024-02-20T14:30:00")
	LocalDateTime createdAt,

	@Schema(description = "수정일시", example = "2024-02-20T14:30:00")
	LocalDateTime updatedAt,

	@Schema(description = "본인 게시글 여부 (로그인 사용자 기준)", example = "false")
	Boolean isMine
) {
}
