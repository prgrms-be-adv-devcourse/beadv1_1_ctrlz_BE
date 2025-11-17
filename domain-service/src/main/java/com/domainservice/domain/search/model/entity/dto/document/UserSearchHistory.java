package com.domainservice.domain.search.model.entity.dto.document;

import java.time.LocalDateTime;

import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchHistory {

	@Field(type = FieldType.Keyword)
	private String userId;

	@Field(type = FieldType.Date, format = DateFormat.date_time)
	private LocalDateTime searchedAt;
}
