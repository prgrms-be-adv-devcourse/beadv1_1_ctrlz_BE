package com.common.model.persistence;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.github.f4b6a3.uuid.UuidCreator;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

	@Id
	private String id;

	@Enumerated(EnumType.STRING)
	private DeleteStatus deleteStatus = DeleteStatus.N;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	protected abstract String getEntitySuffix(); // 각 엔티티가 구현해야 하는 추상 메서드

	@PrePersist // persist() 를 호출하기 전, 새로운 엔티티가 영속성 컨텍스트에 관리되기 직전에 호출
	protected void onCreate() {

		String UUIDv7 = UuidCreator.getTimeOrderedEpoch().toString();
		this.id = UUIDv7 + "-" + getEntitySuffix();

		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	public void delete() {
		this.deleteStatus = DeleteStatus.D;
		this.updatedAt = LocalDateTime.now();
	}

    public void update() {
        this.updatedAt = LocalDateTime.now();
    }
	public void updateTime() {
		this.updatedAt = LocalDateTime.now();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;

		BaseEntity that = (BaseEntity)o;
		return Objects.equals(id, that.id) && Objects.equals(createdAt, that.createdAt);
	}

	@Override
	public int hashCode() {
		int result = Objects.hashCode(id);
		result = 31 * result + Objects.hashCode(createdAt);
		return result;
	}

	public enum DeleteStatus {
		N, D
	}
}
