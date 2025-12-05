package com.domainservice.domain.search.model.entity.dto.document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Document(indexName = "search-words")
@Setting(settingPath = "/elasticsearch/search-words-settings.json")
public class SearchWordDocumentEntity {

	@Id
	private String id;

	@MultiField(
		mainField = @Field(type = FieldType.Text, analyzer = "ngram_analyzer", searchAnalyzer = "nori_search_analyzer"),
		otherFields = {
			@InnerField(suffix = "raw", type = FieldType.Keyword),
			@InnerField(suffix = "nori", type = FieldType.Text, analyzer = "nori_search_analyzer", searchAnalyzer = "nori_search_analyzer")
		}
	)
	private String koreanWord;

	@MultiField(
		mainField = @Field(type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "autocomplete_search_analyzer"),
		otherFields = @InnerField(suffix = "raw", type = FieldType.Keyword)
	)
	private String qwertyInput;

	@MultiField(
		mainField = @Field(type = FieldType.Text, analyzer = "nori_search_analyzer", searchAnalyzer = "nori_search_analyzer"),
		otherFields = @InnerField(suffix = "raw", type = FieldType.Keyword)
	)
	private String category;

	@MultiField(
		mainField = @Field(type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "autocomplete_search_analyzer"),
		otherFields = @InnerField(suffix = "raw", type = FieldType.Keyword)
	)
	private List<String> suggestions;

	@Field(name = "created_at", type = FieldType.Date, format = DateFormat.date_time)
	private LocalDateTime createdAt;

	@Field(name = "recent_searched_at", type = FieldType.Date, format = DateFormat.date_time)
	private LocalDateTime recentSearchedAt;

	@Builder
	private SearchWordDocumentEntity(String id, String koreanWord, String qwertyInput, LocalDateTime recentSearchedAt,
		String category, List<String> suggestions, LocalDateTime createdAt) {

		this.id = id;
		this.koreanWord = koreanWord;
		this.qwertyInput = qwertyInput;
		this.recentSearchedAt = recentSearchedAt;
		this.category = category;
		this.suggestions = suggestions;
		this.createdAt = createdAt;
	}

	public static SearchWordDocumentEntity createDocumentEntity(
		String koreanWord,
		String qwertyInput
	) {
		LocalDateTime now = LocalDateTime.now();

		return SearchWordDocumentEntity.builder()
			.id(UUID.randomUUID().toString())
			.koreanWord(koreanWord)
			.qwertyInput(qwertyInput)
			.category("기타")
			.suggestions(new ArrayList<>())
			.createdAt(now)
			.recentSearchedAt(now)
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SearchWordDocumentEntity that = (SearchWordDocumentEntity)o;
		return Objects.equals(koreanWord, that.koreanWord);
	}

	@Override
	public int hashCode() {
		return Objects.hash(koreanWord);
	}
}
