package com.domainservice.domain.search.model.entity.dto.document;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.domainservice.domain.search.model.entity.persistence.SearchWordLog;
import com.domainservice.domain.search.service.converter.PrefixConverter;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Document(indexName = "search-words")
@Setting(settingPath = "/elasticsearch/search-words-settings.json")
public class SearchWordDocumentEntity {

	@Id
	@MultiField(
		mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer", searchAnalyzer = "nori_search_analyzer"),
		otherFields = {
			@InnerField(suffix = "raw", type = FieldType.Keyword),
			@InnerField(suffix = "nori", type = FieldType.Text, analyzer = "nori_search_analyzer", searchAnalyzer = "nori_search_analyzer"),
			@InnerField(suffix = "eng", type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
		}
	)
	private String originValue;

	@MultiField(
		mainField = @Field(type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "autocomplete_search_analyzer"),
		otherFields = @InnerField(suffix = "raw", type = FieldType.Keyword)
	)
	private String qwertyInput;

	@Field(
		name = "created_at",
		type = FieldType.Date,
		pattern = "yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
	)
	private LocalDateTime createdAt;

	@Field(
		name = "recent_searched_at",
		type = FieldType.Date,
		pattern = "yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
	)
	private LocalDateTime recentSearchedAt;

	@Builder
	private SearchWordDocumentEntity(String originValue, String qwertyInput, LocalDateTime recentSearchedAt,
		LocalDateTime createdAt) {
		this.originValue = originValue;
		this.qwertyInput = qwertyInput;
		this.recentSearchedAt = recentSearchedAt;
		this.createdAt = createdAt;
	}

	public static SearchWordDocumentEntity createDocumentEntity(
		SearchWordLog log
	) {
		String originValue = log.getWord();
		LocalDateTime createdAt = log.getCreatedAt() == null ? log.getSearchedAt() : log.getCreatedAt();
		return SearchWordDocumentEntity.builder()
			.originValue(originValue)
			.qwertyInput(PrefixConverter.convertToQwertyInput(originValue))
			.createdAt(createdAt)
			.recentSearchedAt(log.getSearchedAt())
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SearchWordDocumentEntity that = (SearchWordDocumentEntity)o;
		return Objects.equals(originValue, that.originValue);
	}

	@Override
	public int hashCode() {
		return Objects.hash(originValue);
	}
}
