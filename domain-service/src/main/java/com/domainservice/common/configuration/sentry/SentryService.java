package com.domainservice.common.configuration.sentry;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 500번대 서버 에러 발생 시 예외 정보를 Sentry에 전송하여 에러 추적 및 분석을 수행하는 서비스
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "sentry.enabled", havingValue = "true")
public class SentryService {

	/**
	 * Sentry에 예외 캡처
	 */
	public void captureSentryException(Exception exception, int statusCode) {
		try {
			Sentry.withScope(scope -> {

				// 태그 설정
				scope.setTag("http.status_code", String.valueOf(statusCode));
				scope.setTag("error.type", exception.getClass().getSimpleName());

				// 요청 정보 추가
				ServletRequestAttributes attributes =
					(ServletRequestAttributes)RequestContextHolder.getRequestAttributes();

				if (attributes != null) {
					HttpServletRequest request = attributes.getRequest();
					scope.setExtra("endpoint",
						String.format("%s %s", request.getMethod(), request.getRequestURL()));

					String userId = request.getHeader("X-REQUEST-ID");
					if (userId != null && !"anonymous".equals(userId)) {
						scope.setExtra("user_id", userId);
					}
				}

				// sentry로 예외 내용 전송
				Sentry.captureException(exception);
			});
		} catch (Exception e) {
			log.error("Sentry로 예외 전송 실패", e);
		}
	}
}