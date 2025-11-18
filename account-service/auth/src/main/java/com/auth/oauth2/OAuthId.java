package com.auth.oauth2;

import java.util.Map;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OAuthId {

	GOOGLE("google", "구글");

	private final String registrationId;
	private final String description;

	public static OAuth2UserInfo supplyUserInfo(String registrationId, Map<String, Object> attributes) {

		if (registrationId.equalsIgnoreCase(GOOGLE.name())) {
			return new GoogleOAuth2UserInfo(attributes);
		}
		throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 제공자입니다: " + registrationId);
	}
}
