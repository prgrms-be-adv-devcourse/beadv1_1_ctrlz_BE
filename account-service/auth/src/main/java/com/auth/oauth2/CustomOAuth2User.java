package com.auth.oauth2;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.user.domain.model.User;
import com.user.domain.vo.UserRole;

/**
 * Spring Security의 OAuth2User를 구현
 * - OAuth2User 정보 포함
 */
public record CustomOAuth2User(
	String userId,
	String email,
	List<UserRole> roles,
	String nickname,
	Map<String, Object> attributes
)
	implements OAuth2User {

	public static CustomOAuth2User create(User user, Map<String, Object> attributes) {
		return new CustomOAuth2User(
			user.getId(),
			user.getEmail(),
			user.getRoles(),
			user.getNickname(),
			attributes
		);
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return AuthorityUtils.createAuthorityList(
			roles.stream().map(Enum::name).toList()
		);
	}

	/**
	 * 이메일로 사용
	 */
	@Override
	public String getName() {
		return (String) attributes.get("email");
	}
}
