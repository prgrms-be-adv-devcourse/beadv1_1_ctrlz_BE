package com.aiservice.domain.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
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
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_behavior")
public class UserBehavior {

	@Id
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	private String id;

	@Column(nullable = false, name = "user_id")
	private String userId;

	@Column(nullable = false, name = "behavior_value")
	private String value;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "behavior_type")
	private UserBehaviorType type;

	@CreatedDate
	@Column(updatable = false, name = "created_at")
	private LocalDateTime createdAt;

	@Builder
	public UserBehavior(String userId, String value, UserBehaviorType type) {
		this.userId = userId;
		this.value = value;
		this.type = type;
	}
}
