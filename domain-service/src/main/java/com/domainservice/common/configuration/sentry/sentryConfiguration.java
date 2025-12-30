package com.domainservice.common.configuration.sentry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.sentry.SentryEvent;
import io.sentry.SentryOptions.BeforeSendCallback;
import lombok.extern.slf4j.Slf4j;

/**
 * 500번대 서버 에러만 선별하여 Sentry로 전송하고, 나머지 에러는 필터링하는 설정 클래스
 * 에러가 Sentry 서버로 발송되기 직전에 가로채서 HTTP 상태 코드가 500번대(서버 오류)인 경우만 통과시키는 필터링 역할을 수행합니다.
 */
@Slf4j
@Configuration
public class sentryConfiguration {

	@Bean
	public BeforeSendCallback beforeSendCallback() {
		return (event, hint) -> {

			Integer statusCode = extractStatusCode(event, hint);

			if (statusCode != null) {
				// 500번대 에러만 전송 나머지는 필터링
				if (statusCode >= 500 && statusCode < 600) {
					log.info("Sentry로 {}번대 에러 전송 중: {}", statusCode / 100, event.getMessage());
					return event;
				}
				return null;
			}

			// 상태 코드가 없는 예상치 못한 에러는 전송
			log.info("Sentry로 예상치 못한 에러 전송 중: {}", event.getMessage());
			return event;
		};
	}

	private Integer extractStatusCode(SentryEvent event, Object hint) {
		// 1. Event의 Extra 데이터에서 확인
		Object statusCodeExtra = event.getExtra("statusCode");
		if (statusCodeExtra instanceof Integer) {
			return (Integer)statusCodeExtra;
		}

		// 2. Event의 Tag에서 확인
		String statusTag = event.getTag("http.status_code");
		if (statusTag != null) {
			try {
				return Integer.parseInt(statusTag);
			} catch (NumberFormatException e) {
				log.warn("잘못된 상태 코드 태그: {}", statusTag);
			}
		}

		return null;
	}
}
