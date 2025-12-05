package com.domainservice.domain.search.model.entity.persistence;

import java.time.LocalDateTime;

import com.common.model.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString
@EqualsAndHashCode
@Table(name = "search_word_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchWordLog extends BaseEntity {

	@Column(name = "searched_at")
	private LocalDateTime searchedAt;
	private String word;

	@Builder
	private SearchWordLog(LocalDateTime searchedAt, String word) {
		this.searchedAt = searchedAt;
		this.word = word;
	}

	public static SearchWordLog create(String keyword, LocalDateTime searchedAt) {
		return SearchWordLog.builder()
			.word(keyword)
			.searchedAt(searchedAt)
			.build();
	}

	@Override
	protected String getEntitySuffix() {
		return "search--word-log";
	}
}
