package com.aiservice.application.command;

import java.util.List;

public record CreateProductVectorCommand(
	String productId,
	String title,
	String name,
	String categoryName,
	String status,
	int price,
	String description,
	List<String> tags
) {

	public CreateProductVectorCommand {

		if (productId == null || productId.isBlank()) {
			throw new IllegalArgumentException("productId는 필수 값입니다.");
		}

		title = defaultIfNull(title);
		name = defaultIfNull(name);
		categoryName = defaultIfNull(categoryName);
		status = defaultIfNull(status);
		description = defaultIfNull(description);
		tags = (tags == null) ? List.of() : tags;
		if (price == 0 || price < 0) {
			throw new IllegalArgumentException("price는 음수 일 수 없습니다.");
		}
	}

	private String defaultIfNull(String value) {
		return value == null ? "" : value;
	}

}
