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

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Account Service Security 설정
 * - OAuth2는 Gateway에서 처리하므로 제거
 * - 단순 API 서버로만 동작
 * - Gateway로부터 들어오는 요청만 처리
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		return http
			// CSRF 비활성화 (Stateless API)
			.csrf(AbstractHttpConfigurer::disable)

			// CORS 설정
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))

			// 프레임 옵션 설정 (H2 콘솔용)
			.headers(headers -> headers
				.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
			)

			// 기본 인증 및 폼 로그인 비활성화
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)

			// 세션 사용 안 함 (Stateless)
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
			
			// 모든 요청 허용 (Gateway에서 인증/인가 처리)
			.authorizeHttpRequests(auth -> auth
				.anyRequest().permitAll()
			)

			.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		// Gateway와 프론트엔드 주소 허용
		configuration.setAllowedOrigins(List.of(
			"http://localhost:8082",  // Gateway
			"http://localhost:3000"   // Frontend
		));

		configuration.setAllowedMethods(List.of(
			HttpMethod.GET.name(),
			HttpMethod.POST.name(),
			HttpMethod.PUT.name(),
			HttpMethod.PATCH.name(),
			HttpMethod.DELETE.name(),
			HttpMethod.OPTIONS.name()
		));

		configuration.setAllowedHeaders(List.of(
			HttpHeaders.AUTHORIZATION,
			HttpHeaders.CONTENT_TYPE,
			"X-Requested-With",
			"X-USER-ID",
			"X-REQUEST-ID"
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