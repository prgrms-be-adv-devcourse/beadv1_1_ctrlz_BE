package com.domainservice.common.configuration.sentry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.sentry.SentryEvent;
import io.sentry.SentryOptions.BeforeSendCallback;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class sentryConfiguration {

	/**
	 * 500번대 에러만 Sentry에 전송하도록 필터링
	 */
	@Bean
	public BeforeSendCallback beforeSendCallback() {
		return (event, hint) -> {

			// HTTP 상태 코드 추출
			Integer statusCode = extractStatusCode(event, hint);

			if (statusCode != null) {
				// 500번대 에러만 전송
				if (statusCode >= 500 && statusCode < 600) {
					log.info("Sentry로 {}번대 에러 전송 중: {}", statusCode / 100, event.getMessage());
					return event;
				}

				// 400번대 에러는 필터링
				log.debug("Sentry 전송 제외: {}번대 에러 - {}", statusCode / 100, event.getMessage());
				return null;
			}

			// 상태 코드가 없는 예상치 못한 에러는 전송
			log.info("Sentry로 예상치 못한 에러 전송 중: {}", event.getMessage());
			return event;
		};
	}

	/**
	 * Event와 Hint에서 HTTP 상태 코드 추출
	 */
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
