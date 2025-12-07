package com.search.configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.batch.item.file.LineMapper;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
import com.search.dto.SearchHistoryDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchLogLineMapper implements LineMapper<SearchHistoryDto> {

    private final ObjectMapper objectMapper;

    private static final Pattern QUERY_PATTERN = Pattern.compile("query\\s*=\\s*\"?([^,\"]+)\"?");
    private static final Pattern USER_ID_PATTERN = Pattern.compile("userId\\s*=\\s*\"?([^,\"]+)\"?");

    @Override
    public SearchHistoryDto mapLine(String line, int lineNumber) throws Exception {
        try {
            JsonNode root = objectMapper.readTree(line);
            String timestamp = root.get("@timestamp").asText();
            String data = root.get("data").asText();

            String query = extractValue(QUERY_PATTERN, data);
            String userId = extractValue(USER_ID_PATTERN, data);

            validateExtractedValues(query, userId, lineNumber, data);

            LocalDateTime createdAt = parseTimestamp(timestamp, lineNumber);

            return SearchHistoryDto.builder()
                    .id(UuidCreator.getTimeOrderedEpoch().toString())
                    .userId(userId)
                    .searchTerm(query)
                    .createdAt(createdAt)
                    .build();

        } catch (Exception e) {
            log.warn("라인 {} 처리 중 오류 발생: {}", lineNumber, e.getMessage());
            throw e;
        }
    }

    private String extractValue(Pattern pattern, String data) {
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private void validateExtractedValues(String query, String userId, int lineNumber, String data) {
        if (query == null || userId == null || query.isEmpty() || userId.isEmpty()) {
            log.warn("라인 {}: query 또는 userId 누락 - data: {}", lineNumber, data);
            throw new IllegalArgumentException("query 또는 userId가 누락되었습니다");
        }
    }

    private LocalDateTime parseTimestamp(String timestamp, int lineNumber) {
        try {
            return LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            log.warn("라인 {}: 잘못된 타임스탬프 형식 - {}", lineNumber, timestamp);
            throw e;
        }
    }
}
