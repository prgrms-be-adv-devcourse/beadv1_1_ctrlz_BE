package com.gatewayservice.client;

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import com.gatewayservice.dto.LoginResponse;
import com.gatewayservice.dto.LoginRequest;
import com.gatewayservice.exception.ExternalServiceException;

import io.netty.handler.timeout.TimeoutException;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountServiceClient {

	private final WebClient.Builder webClientBuilder;

	@Value("${services.account.url:lb://ACCOUNT-SERVICE}")
	private String accountServiceUrl;

	public Mono<LoginResponse> processLogin(LoginRequest request) {
		log.info("Account Service 호출: {}", request.email());

		return webClientBuilder.build()
			.post()
			.uri(accountServiceUrl + "/api/auth/login")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(request)
			//응답을 받아서 처리
			.retrieve()
			//400 이상인지 체크
			.onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
				clientResponse.bodyToMono(ErrorResponse.class)
					.flatMap(error -> Mono.error(new BadRequestException(
						Optional.ofNullable(error.getBody().getDetail())
							.orElse("잘못된 요청입니다."))))
			)
			//500 이상인지 체크
			.onStatus(HttpStatusCode::is5xxServerError, serverResponse ->
				serverResponse.bodyToMono(String.class)
					.flatMap(body -> {
						log.error("Account Service 5xx 오류: {}", body);
						return Mono.error(new ExternalServiceException("Account Service 오류"));
					})
			)
			//성공 바디를 LoginResponse 객체로 자동 역직렬화
			.bodyToMono(LoginResponse.class)
			.timeout(Duration.ofSeconds(3))
			.onErrorMap(TimeoutException.class,
				e -> new ExternalServiceException("Account Service Timeout"))
			.retryWhen( //재시도
				Retry.backoff(3, Duration.ofMillis(300)) // 최대 3회, 300ms 지수 백오프
					.filter(throwable ->
						throwable instanceof ExternalServiceException ||       // 서버 오류 5xx
							throwable instanceof WebClientRequestException         // 네트워크 오류
					)
					.onRetryExhaustedThrow((spec, signal) ->
						new ExternalServiceException("Account Service 재시도 초과")
					)
			)
			//성공은 했으나 응답이 없을경우
			.switchIfEmpty(Mono.error(new ExternalServiceException("Account Service 응답이 없습니다.")))
			//성공했을 때만 실행
			//Mono 전용
			.doOnSuccess(res -> log.info("Account Service 응답 성공: userId={}", res.userId()))
			//타임아웃이 났을 경우
			//최종적으로 에러가 났을 때 로그 (onStatus에서 잡은 것도 여기로 옴)
			.doOnError(e -> log.error("Account Service 호출 실패", e));
	}
}
