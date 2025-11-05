package com.common.model.persistence;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    private DeleteStatus deleteStatus = DeleteStatus.N;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected abstract String getEntityPrefix(); // 각 엔티티가 구현해야 하는 추상 메서드

    @PrePersist // persist() 를 호출하기 전, 새로운 엔티티가 영속성 컨텍스트에 관리되기 직전에 호출
    protected void onCreate() {

        String UUIDv7 = UuidCreator.getTimeOrderedEpoch().toString();
        this.code = getEntityPrefix() + "-" + UUIDv7;

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

    }

    public void delete() {
        this.deleteStatus = DeleteStatus.D;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }

    public enum DeleteStatus {
        N, D
    }

    /* Getter */
    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public DeleteStatus getDeleteStatus() {
        return deleteStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
