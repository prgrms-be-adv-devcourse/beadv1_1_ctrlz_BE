package com.user.infrastructure.jpa.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Entity;
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

	private String eventType;

	private String commandType;

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
		String eventType,
		String commandType,
		String content,
		boolean published,
		LocalDateTime published_at,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
	) {
		this.id = id;
		this.userId = userId;
		this.eventType = eventType;
		this.commandType = commandType;
		this.content = content;
		this.published = published;
		this.published_at = published_at;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static ExternalEventEntity from(String userId, String eventType, String commandType, String content) {
		return ExternalEventEntity.builder()
			.userId(userId)
			.eventType(eventType)
			.content(content)
			.published(false)
			.commandType(commandType)
			.published_at(null)
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();
	}

	public void publishedComplete() {
		this.published = true;
		this.published_at = LocalDateTime.now();
	}
}
