package com.aiservice.domain.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "search_history")
public class SearchHistory {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	private Long id;

	@Column(nullable = false, name = "user_id")
	private String userId;

	@Column(nullable = false, name = "search_term")
	private String searchTerm;

	@CreatedDate
	@Column(updatable = false, name = "created_at")
	private LocalDateTime createdAt;

	@Builder
	public SearchHistory(String userId, String searchTerm) {
		this.userId = userId;
		this.searchTerm = searchTerm;
	}
}
