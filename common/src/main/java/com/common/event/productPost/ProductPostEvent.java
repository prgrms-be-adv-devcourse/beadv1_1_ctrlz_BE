package com.common.event.productPost;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPostEvent {

	private String id;
	private String name;
	private String title;
	private String description;
	private List<String> tags;
	private String categoryName;
	private Long price;
	private Long likedCount;
	private Long viewCount;
	private String status;
	private String tradeStatus;
	private String deleteStatus;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;

	private EventType eventType;

	public enum EventType {
		CREATE, UPDATE, DELETE
	}
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