package com.domainservice.common.exception;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.domainservice.common.configuration.sentry.SentryService;
import com.domainservice.common.configuration.sentry.SlackWebhookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 500번대 서버 에러 발생 시 Sentry와 Slack으로 에러 알림을 전송하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorNotificationService {

	private final Optional<SentryService> sentryService;
	private final Optional<SlackWebhookService> slackWebhookService;

	/**
	 * 500번대 서버 에러 알림 전송
	 */
	public void notifyError(Exception exception, int statusCode) {

		if (!(statusCode >= 500 && statusCode < 600)) {
			return;
		}

		sendToSentry(exception, statusCode);
		sendToSlack(exception);
	}

	/**
	 * Sentry로 예외 전송
	 */
	private void sendToSentry(Exception exception, int statusCode) {
		sentryService.ifPresent(sentryService -> {
			try {
				sentryService.captureSentryException(exception, statusCode);
			} catch (Exception e) {
				log.error("Sentry 전송 중 예상치 못한 오류 발생", e);
			}
		});
	}

	/**
	 * Slack으로 알림 전송
	 */
	private void sendToSlack(Exception exception) {
		slackWebhookService.ifPresent(slackWebhookService -> {
			try {
				slackWebhookService.sendErrorNotification(exception);
			} catch (Exception e) {
				log.error("Slack 전송 중 예상치 못한 오류 발생", e);
			}
		});
	}

}