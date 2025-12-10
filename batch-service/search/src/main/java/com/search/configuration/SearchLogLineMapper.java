package com.search.configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.batch.item.file.LineMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
import com.search.dto.UserBehaviorDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchLogLineMapper implements LineMapper<UserBehaviorDto> {

    private final ObjectMapper objectMapper;
    private final String behaviorType;

    // Pattern을 상수로 선언하여 재사용 (성능 최적화)
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("(\\w+)\\s*=\\s*([^,]+)");

    public SearchLogLineMapper(String behaviorType) {
        this.objectMapper = new ObjectMapper();
        this.behaviorType = behaviorType;
    }

    @Override
    public UserBehaviorDto mapLine(String line, int lineNumber) throws Exception {
        try {
            JsonNode jsonNode = objectMapper.readTree(line);
            JsonNode dataNode = jsonNode.get("data");

            validateDataNode(dataNode, lineNumber);

            // userId는 JSON 루트 레벨에서 먼저 확인 (item-view.log)
            // 없으면 data 내부에서 파싱 (search-view.log)
            String userIdFromRoot = jsonNode.path("userId").asText(null);

            ParsedData parsedData = parseDataNode(dataNode, userIdFromRoot);
            validateParsedValue(parsedData.value(), lineNumber, dataNode);

            String timestamp = jsonNode.get("@timestamp").asText();

            // ANONYMOUS 사용자는 skip
            validateUserId(parsedData.userId(), lineNumber);

            return buildUserBehaviorDto(parsedData, timestamp);

        } catch (JsonProcessingException e) {
            log.warn("라인 {} JSON 파싱 오류: {}", lineNumber, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.warn("라인 {} 매핑 중 오류 발생: {}", lineNumber, e.getMessage());
            throw e;
        }
    }

    /**
     * data 노드 유효성 검증
     */
    private void validateDataNode(JsonNode dataNode, int lineNumber) {
        if (dataNode == null) {
            throw new IllegalArgumentException(
                    String.format("Line %d: Invalid log format - 'data' field missing", lineNumber));
        }
    }

    /**
     * data 노드 파싱 (텍스트 또는 JSON 객체 형식 지원)
     */
    private ParsedData parseDataNode(JsonNode dataNode, String userId) {
        if (dataNode.isTextual()) {
            return parseTextualData(dataNode.asText(), userId);
        } else {
            return parseJsonData(dataNode, userId);
        }
    }

    /**
     * 텍스트 형식의 data 파싱 ("key = value, key2 = value2" 형식)
     */
    private ParsedData parseTextualData(String dataText, String userIdFromRoot) {
        log.debug("Parsing data string: {}", dataText);

        String userId = userIdFromRoot; // 먼저 루트 레벨 userId 사용
        String value = null;

        Matcher matcher = KEY_VALUE_PATTERN.matcher(dataText);

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String val = matcher.group(2).trim();

            // data 내부에 userId가 있으면 사용 (search-view.log 케이스)
            if ("userId".equals(key)) {
                userId = val;
            } else if ("SEARCH".equals(behaviorType) && "query".equals(key)) {
                value = val;
            } else if ("VIEW".equals(behaviorType) && "title".equals(key)) {
                value = val;
            }
        }

        return new ParsedData(userId, value);
    }

    /**
     * JSON 객체 형식의 data 파싱
     */
    private ParsedData parseJsonData(JsonNode dataNode, String userId) {
        String value;
        if ("SEARCH".equals(behaviorType)) {
            value = dataNode.path("query").asText(null);
        } else if ("VIEW".equals(behaviorType)) {
            value = dataNode.path("title").asText(null);
        } else {
            value = dataNode.path("value").asText(null);
        }

        // userId는 이미 파라미터로 전달받음
        return new ParsedData(userId, value);
    }

    /**
     * 파싱된 값 유효성 검증
     */
    private void validateParsedValue(String value, int lineNumber, JsonNode dataNode) {
        if (value == null || value.isEmpty()) {
            log.warn("Line {}: Value is missing for type {}. Data: {}", lineNumber, behaviorType, dataNode);
            throw new IllegalArgumentException("Missing value for behavior type: " + behaviorType);
        }
    }

    /**
     * userId 유효성 검증 (ANONYMOUS는 skip)
     */
    private void validateUserId(String userId, int lineNumber) {
        if (userId == null || userId.isEmpty() || "ANONYMOUS".equalsIgnoreCase(userId)) {
            log.debug("Line {}: ANONYMOUS 또는 빈 userId - skip 처리", lineNumber);
        }
    }

    /**
     * UserBehaviorDto 빌드
     */
    private UserBehaviorDto buildUserBehaviorDto(ParsedData parsedData, String timestamp) {
        return UserBehaviorDto.builder()
                .id(UuidCreator.getTimeOrderedEpoch().toString())
                .userId(parsedData.userId())
                .behaviorValue(parsedData.value())
                .behaviorType(behaviorType)
                .createdAt(LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }

    private record ParsedData(String userId, String value) {
    }
}
