package com.userservice.infrastructure.model.entity;

import com.userservice.domain.vo.UserRole;
import com.userservice.infrastructure.adapter.out.BaseEntity;
import com.userservice.infrastructure.model.vo.EmbeddedAddress;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity {

	@Id
	private String id;

	private String OAuthId;
	private String name;
	private String nickname;
	private String email;
	private String password;

	//TODO: 판매자 등록시 인증??
	@Enumerated(EnumType.STRING)
	private UserRole role = UserRole.USER;

	private String profileUrl;
	private String phoneNumber;

	@Embedded
	private EmbeddedAddress address;

	@Builder
	public UserEntity(
		String id,
		String name,
		String email,
		String password,
		UserRole role,
		String profileUrl,
		String phoneNumber,
		EmbeddedAddress address,
		String oauthId,
		String nickname
	) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.password = password;
		this.role = role;
		this.profileUrl = profileUrl;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.id = id;
		this.nickname = nickname;
	}
}
