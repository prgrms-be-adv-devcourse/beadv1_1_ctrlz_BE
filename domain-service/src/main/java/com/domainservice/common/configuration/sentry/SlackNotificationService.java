package com.domainservice.common.configuration.sentry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 500번대 서버 에러 발생 시 Slack Webhook을 통해 실시간 알림을 전송하는 서비스
 * Sentry와 함께 사용되어, Sentry는 에러 추적 및 분석 용도로,
 * 이 서비스는 팀원들에게 즉각적인 알림 전달 용도로 사용됩니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "slack.webhook.enabled", havingValue = "true")
public class SlackNotificationService {

	@Value("${slack.webhook.url}")
	private String webhookUrl;

	private final RestTemplate restTemplate = new RestTemplate();

	/**
	 * Slack으로 에러 알림 전송
	 */
	public void sendErrorNotification(Exception exception, String requestUrl, String httpMethod, String userId) {

		// 메시지 구성 요소 준비
		String userInfo = (userId != null && !"anonymous".equals(userId)) ? String.format("`%s`", userId) : "인증되지 않음";
		String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

		String errorMessage = escape(exception.getMessage());
		String exceptionType = exception.getClass().getSimpleName();

		String stackTrace = Arrays.stream(exception.getStackTrace())
			.limit(5)
			.map(StackTraceElement::toString)
			.collect(Collectors.joining("\n"));

		String endpoint = String.format("%s %s",
			httpMethod != null ? httpMethod : "N/A",
			requestUrl != null ? requestUrl : "N/A");

		// 구성 요소를 기반으로 JSON 본문 생성
		String payload = createSlackPayload(
			errorMessage, endpoint, exceptionType, userInfo, currentTime, escape(stackTrace)
		);

		// Slack 전송
		sendToSlack(payload);
	}

	// Slack에 전송할 JSON 형태의 페이로드 생성
	private String createSlackPayload(
		String errorMessage, String endpoint, String exceptionType,
		String userInfo, String currentTime, String stackTrace
	) {
		return """
			{
			  "blocks": [
			    {
			      "type": "header",
			      "text": {
			        "type": "plain_text",
			        "text": "🚨 서버 에러 발생",
			        "emoji": true
			      }
			    },
			    {
			      "type": "section",
			      "fields": [
			        {"type": "mrkdwn", "text": "*서비스:*\\n`domain-service`"},
			        {"type": "mrkdwn", "text": "*에러 메시지:*\\n%s"},
			        {"type": "mrkdwn", "text": "*엔드포인트:*\\n`%s`"},
			        {"type": "mrkdwn", "text": "*예외 타입:*\\n`%s`"},
			        {"type": "mrkdwn", "text": "*사용자 ID:*\\n%s"},
			        {"type": "mrkdwn", "text": "*발생 시각:*\\n%s"}
			      ]
			    },
			    {"type": "divider"},
			    {
			      "type": "section",
			      "text": {
			        "type": "mrkdwn",
			        "text": "*스택 트레이스 (상위 5개):*\\n```%s```"
			      }
			    },
			    {
			      "type": "context",
			      "elements": [
			        {
			          "type": "mrkdwn",
			          "text": "상세 내역은 <https://sentry.io|팀 sentry.io> 에서 확인해주세요."
			        }
			      ]
			    }
			  ]
			}
			""".formatted(
			errorMessage,
			endpoint,
			exceptionType,
			userInfo,
			currentTime,
			stackTrace
		);
	}

	// Slack Webhook으로 메시지 전송
	private void sendToSlack(String payload) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(payload, headers);

		try {
			restTemplate.postForEntity(webhookUrl, request, String.class);
			log.info("Slack 에러 알림 전송 완료");
		} catch (Exception e) {
			log.error("Slack 전송 실패: {}", e.getMessage());
		}
	}

	// JSON 문자열 내 특수 문자 이스케이프 처리
	private String escape(String input) {
		if (input == null) {
			return "정보 없음";
		}
		return input
			.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("\n", "\\n")
			.replace("\r", "");
	}
}