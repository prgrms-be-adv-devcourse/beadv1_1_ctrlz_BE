package com.auth.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsUtils;

import com.auth.handler.OAuth2SuccessHandler;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfiguration {

	@Bean
	public SecurityFilterChain securityFilterChain(
		HttpSecurity http
	) throws Exception {

		return http

			.csrf(AbstractHttpConfigurer::disable)
			.cors(Customizer.withDefaults())

			.headers(headers -> headers
				.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
			)
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.cors(Customizer.withDefaults())
			.oauth2Login(i -> {
				i.successHandler(new OAuth2SuccessHandler());
				i.failureHandler((req, resp, e) -> {});
				i.loginProcessingUrl("/auth/login");
			})
			.sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint((request, response, authException) -> {
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증에 실패하였습니다.");
				})
			)

			.authorizeHttpRequests(
				auth -> auth
					.requestMatchers("/favicon.ico").permitAll()
					.requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
					.requestMatchers("/h2-console/**").permitAll()
					.anyRequest().permitAll()
			)

			.build();

	}

}