// package com.domainservice.domain.search.model.entity.dto.document;
//
// import java.time.LocalDateTime;
// import java.util.HashSet;
// import java.util.List;
//
// import org.springframework.data.annotation.Id;
// import org.springframework.data.elasticsearch.annotations.DateFormat;
// import org.springframework.data.elasticsearch.annotations.Document;
// import org.springframework.data.elasticsearch.annotations.Field;
// import org.springframework.data.elasticsearch.annotations.FieldType;
// import org.springframework.data.elasticsearch.annotations.InnerField;
// import org.springframework.data.elasticsearch.annotations.MultiField;
// import org.springframework.data.elasticsearch.annotations.Setting;
//
// import lombok.AllArgsConstructor;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
//
// @Getter
// @AllArgsConstructor
// @NoArgsConstructor
// @Document(indexName = "search-words")
// @Setting(settingPath = "/elasticsearch/search-words-settings.json")
// public class SearchWordDocumentEntity {
//
// 	@Id
// 	private String id;
//
// 	@MultiField(
// 		mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_synonym_analyzer"),
// 		otherFields = {
// 			@InnerField(suffix = "raw", type = FieldType.Keyword)
// 		}
// 	)
// 	private String koreanWord; //컴퓨터
//
// 	@MultiField(
// 		mainField = @Field(type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "autocomplete_search_analyzer"),
// 		otherFields = {
// 			@InnerField(suffix = "raw", type = FieldType.Keyword)
// 		}
// 	)
// 	private String qwertyInput;	//zjavbxj
//
// 	@Field(type = FieldType.Long)
// 	private Long searchedCount; // 얼마나?
//
// 	@Field(name = "recent_searched_at", type = FieldType.Date, format = DateFormat.date_time)
// 	private LocalDateTime recentSearchedAt; // 가장 최근 조회 언제?
//
// 	// @Field(name = "daily_count", type = FieldType.Long)
// 	// private Long dailyCount; // 오늘 얼마나? -> Redis에 저장하는 것은 어떤지?
// 	//
// 	// @Field(name = "weekly_count", type = FieldType.Long)
// 	// private Long weeklyCount; // 주간 얼마나? -> Redis에 저장하는 것은 어떤지?
//
// 	@MultiField(
// 		mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_synonym_analyzer"),
// 		otherFields = {
// 			@InnerField(suffix = "raw", type = FieldType.Keyword)
// 		}
// 	)
// 	private String category; // 무슨 카테고리에 포함됨?
//
// 	@MultiField(
// 		mainField = @Field(type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "autocomplete_search_analyzer"),
// 		otherFields = {
// 			@InnerField(suffix = "raw", type = FieldType.Keyword)
// 		}
// 	)
// 	private List<String> suggestions; //검색어 자동완성 느낌의 변수로 변경
//
// 	// @Field(name = "related_words", type = FieldType.Nested)
// 	// private List<UserSearchHistory> userHistory; // -> Redis에 저장하는 것은 어떤지?
//
// 	@Field(name = "trend_score", type = FieldType.Double)
// 	private Double trendScore;
//
// 	@Field(name = "created_at", type = FieldType.Date, format = DateFormat.date_time)
// 	private LocalDateTime createdAt;
//
// 	@Field(name = "updated_at", type = FieldType.Date, format = DateFormat.date_time)
// 	private LocalDateTime updatedAt;
// }
