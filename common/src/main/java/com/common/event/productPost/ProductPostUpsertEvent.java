package com.common.event.productPost;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
public record ProductPostUpsertEvent(
	String id,
	String name,
	String title,
	String description,
	List<String> tags,
	String categoryName,
	Long price,
	Long likedCount,
	Long viewCount,
	String status,
	String tradeStatus,
	String deleteStatus,

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS") // 밀리초 이슈 방지를 위해 .SSS 권장
	LocalDateTime createdAt,

	EventType eventType
) {
}

// TODO: 추후 domain에 있는 enum 값을 common 으로 가져와서 refactoring
@Getter
@RequiredArgsConstructor
enum ProductStatus {
	NEW("새 상품"),
	GOOD("양호"),
	FAIR("보통");

	private final String description;
}

@Getter
@RequiredArgsConstructor
enum TradeStatus {
	SELLING("판매중"),
	PROCESSING("거래중"),
	SOLDOUT("판매완료");

	private final String description;
}