package com.domainservice.domain.search.model.entity.dto.document;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "product-posts")
@Setting(settingPath = "/elasticsearch/product-posts-settings.json")
public class ProductPostDocumentEntity {

	@Id
	private String id;

	@MultiField(
		// nori로 한글 형태소 분석 후 일반 검색에 사용
		mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_synonym_analyzer"),
		otherFields = {
			@InnerField(suffix = "completion", type = FieldType.Search_As_You_Type),
			// Search_As_You_Type 타입으로 실시간 자동완성 전용 (2gram, 3gram 자동 생성)
			@InnerField(suffix = "keyword", type = FieldType.Keyword, normalizer = "lowercase_normalizer"),
			// 대소문자 정규화 후 정확한 일치 검색/필터링용
			@InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "edge_ngram_analyzer", searchAnalyzer = "nori_synonym_analyzer")
			// edge ngram으로 부분 일치 검색 지원 (1~20자)
		}
	)
	private String name;

	@MultiField(
		// nori로 한글 형태소 분석 후 일반 검색에 사용
		mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_synonym_analyzer"),
		otherFields = {
			@InnerField(suffix = "completion", type = FieldType.Search_As_You_Type),
			// Search_As_You_Type 타입으로 실시간 자동완성 전용 (2gram, 3gram 자동 생성)
			@InnerField(suffix = "keyword", type = FieldType.Keyword, normalizer = "lowercase_normalizer"),
			// 대소문자 정규화 후 정확한 일치 검색/필터링용
			@InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "edge_ngram_analyzer", searchAnalyzer = "nori_synonym_analyzer")
			// edge ngram으로 부분 일치 검색 지원 (1~20자)
		}
	)
	private String title;

	// HTML 태그 제거를 지원하는 커스텀 analyzer를 적용하여, 상품 설명에 포함된 HTML을 제거한 순수 텍스트만 색인
	@MultiField(
		mainField = @Field(type = FieldType.Text, analyzer = "nori_html_analyzer", searchAnalyzer = "nori_synonym_analyzer"),
		otherFields = {
			@InnerField(suffix = "keyword", type = FieldType.Keyword, normalizer = "lowercase_normalizer")
		}
	)
	private String description;

	@Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_synonym_analyzer")
	private List<String> tags;

	@MultiField(
		mainField = @Field(name = "category_name", type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_synonym_analyzer"),
		otherFields = {
			@InnerField(suffix = "keyword", type = FieldType.Keyword, normalizer = "lowercase_normalizer")
		}
	)
	private String categoryName;

	@Field(type = FieldType.Long)
	private Long price;

	// 테이블에 들어가는 이름(liked_count)과 Field name을 안맞춰주면 매핑을 못해서 null 값 가져옴
	// 카멜케이스로 필드 선언했을 때 체크해주기
	@Field(name = "liked_count",type = FieldType.Long)
	private Long likedCount;

	@Field(name = "view_count",type = FieldType.Long)
	private Long viewCount;

	@Field(type = FieldType.Keyword)
	private String status;

	@Field(name = "trade_status",type = FieldType.Keyword)
	private String tradeStatus;

	@Field(name = "delete_status",type = FieldType.Keyword)
	private String deleteStatus;

	@Field(name = "created_at",type = FieldType.Date, format = DateFormat.date_time)
	private LocalDateTime createdAt;

	// @Field(type = FieldType.Date, format = DateFormat.date_time)
	// private LocalDateTime updatedAt;

	// @Field(name = "priority_score", type = FieldType.Double)
	// private Double priorityScore;

	// @Field(name = "account_code", type = FieldType.Keyword)
	// private String accountCode;
}


