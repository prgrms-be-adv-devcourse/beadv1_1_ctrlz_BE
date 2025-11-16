package com.user.infrastructure.jpa.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.user.domain.vo.EventType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_events")
@Entity
public class ExternalEventEntity {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	private String id;

	private String userId;

	@Enumerated(EnumType.STRING)
	private EventType eventType;

	private String content;
	private boolean published;

	private LocalDateTime published_at;

	@CreatedDate
	private LocalDateTime createdAt;

	@LastModifiedDate
	private LocalDateTime updatedAt;

	@Builder
	ExternalEventEntity(
		String id,
		String userId,
		EventType eventType,
		String content,
		boolean published,
		LocalDateTime published_at,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
	) {
		this.id = id;
		this.userId = userId;
		this.eventType = eventType;
		this.content = content;
		this.published = published;
		this.published_at = published_at;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static ExternalEventEntity from(String userId, EventType eventType, String content) {
		return ExternalEventEntity.builder()
			.userId(userId)
			.eventType(eventType)
			.content(content)
			.published(false)
			.published_at(LocalDateTime.now())
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();
	}

	public void publishedComplete() {
		this.published = true;
	}
}
