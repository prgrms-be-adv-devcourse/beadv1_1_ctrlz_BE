package com.auth.service;

import java.util.List;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.auth.oauth2.CustomOAuth2User;
import com.auth.oauth2.OAuth2UserInfo;
import com.auth.oauth2.OAuthId;
import com.user.application.port.out.UserPersistencePort;
import com.user.domain.model.User;
import com.user.domain.vo.UserRole;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OAuth2 로그인 시 사용자 정보를 처리하는 서비스
 * - 이미 추가 정보를 입력했는지를 확인
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserPersistencePort userPersistencePort;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		log.info("CustomOAuth2UserService");

		OAuth2User oAuth2User = super.loadUser(userRequest);
		String registrationId = userRequest.getClientRegistration().getRegistrationId();

		log.info("OAuth2 로그인 시도 - registrationId: {}", registrationId);

		try {
			return processOAuth2User(registrationId, oAuth2User);
		} catch (Exception e) {
			log.error("OAuth2 사용자 처리 중 오류 발생", e);
			throw new OAuth2AuthenticationException("OAuth2 사용자 처리 실패: " + e.getMessage());
		}
	}

	private OAuth2User processOAuth2User(String registrationId, OAuth2User oAuth2User) {
		log.info("attribute: {}", oAuth2User.getAttributes());

		OAuth2UserInfo oAuth2UserInfo = OAuthId.supplyUserInfo(registrationId, oAuth2User.getAttributes());

		if (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isEmpty()) {
			throw new OAuth2AuthenticationException("이메일 정보를 찾을 수 없습니다.");
		}

		User user = userPersistencePort.findByEmailAndOAuthId(
			oAuth2UserInfo.getEmail(),
			registrationId
		).orElseGet(() -> User.builder().roles(List.of(UserRole.USER)).email(oAuth2UserInfo.getEmail()).build());

		log.info("user = {}", user);
		return CustomOAuth2User.create(user, oAuth2User.getAttributes());
	}
}
