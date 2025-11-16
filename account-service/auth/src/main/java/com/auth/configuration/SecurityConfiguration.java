package com.auth.configuration;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.auth.handler.OAuth2FailureHandler;
import com.auth.handler.OAuth2SuccessHandler;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

	private final OAuth2SuccessHandler oAuth2SuccessHandler;
	private final OAuth2FailureHandler oAuth2FailureHandler;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		return http
			// CSRF 비활성화
			.csrf(AbstractHttpConfigurer::disable)

			// CORS 설정
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))

			// 프레임 옵션 설정 (H2 콘솔) -> 추후 제거
			.headers(headers -> headers
				.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
			)

			// 기본 인증 및 폼 로그인 비활성화
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)

			// OAuth2 로그인 설정
			.oauth2Login(oauth2 -> oauth2
				.successHandler(oAuth2SuccessHandler) // 로그인 성공 핸들러
				.failureHandler(oAuth2FailureHandler) // 로그인 실패 핸들러
			)

			// 세션 정책: Stateless (JWT 사용)
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)

			// 예외 처리
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint((request, response, authException) -> {
					log.info("인증 실패: {}", authException.getMessage());
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다.");
				})
				.accessDeniedHandler((request, response, accessDeniedException) -> {
					log.info("접근 거부: {}", accessDeniedException.getMessage());
					response.sendError(HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다.");
				})
			)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/",
					"/error",
					"/favicon.ico",
					"/*.png",
					"/*.gif",
					"/*.svg",
					"/*.jpg",
					"/*.html",
					"/*.css",
					"/*.js"
				).permitAll()
				.requestMatchers(
					"/oauth2/**",
					"/login/**"
				).permitAll()
				// H2 콘솔
				.requestMatchers("/h2-console/**").permitAll()

				.anyRequest().permitAll()
			)

			.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		configuration.setAllowedOrigins(List.of("localhost:8080"));

		configuration.setAllowedMethods(List.of(
			HttpMethod.GET.name(),
			HttpMethod.POST.name(),
			HttpMethod.PUT.name(),
			HttpMethod.PATCH.name(),
			HttpMethod.DELETE.name(),
			HttpMethod.OPTIONS.name(),
			HttpMethod.TRACE.name()
			));

		configuration.setAllowedHeaders(List.of(
			HttpHeaders.AUTHORIZATION,
			HttpHeaders.CONTENT_TYPE,
			"X-Requested-With",
			"X-CODE"
		));

		configuration.setExposedHeaders(List.of(
			HttpHeaders.AUTHORIZATION,
			HttpHeaders.SET_COOKIE
		));

		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}
}