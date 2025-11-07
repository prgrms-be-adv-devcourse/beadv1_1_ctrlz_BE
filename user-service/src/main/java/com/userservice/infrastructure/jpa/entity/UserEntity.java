package com.userservice.infrastructure.jpa.entity;

import java.util.ArrayList;
import java.util.List;

import com.common.model.persistence.BaseEntity;
import com.userservice.domain.vo.UserRole;
import com.userservice.infrastructure.jpa.converter.UserInformationConverter;
import com.userservice.infrastructure.jpa.vo.EmbeddedAddress;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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

	@Column(nullable = false)
	private String oauthId;

	@Column(nullable = false)
	@Convert(converter = UserInformationConverter.class)
	private String name;

	@Column(nullable = false, unique = true)
	private String nickname;

	@Column(nullable = false, unique = true)
	private String email;

	private String password;

	@Enumerated(EnumType.STRING)
	@ElementCollection(fetch = FetchType.EAGER)
	@Column(nullable = false)
	private List<UserRole> roles = new ArrayList<>();

	private String profileUrl;

	@Convert(converter = UserInformationConverter.class)
	@Column(nullable = false)
	private String phoneNumber;

	@Embedded
	private EmbeddedAddress address;

	@Builder
	public UserEntity(
		String name,
		String email,
		String password,
		String profileUrl,
		String phoneNumber,
		EmbeddedAddress address,
		String oauthId,
		String nickname
	) {
		this.oauthId = oauthId;
		this.name = name;
		this.email = email;
		this.password = password;
		this.profileUrl = profileUrl;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.nickname = nickname;

		this.roles.add(UserRole.USER);
	}

	@Override
	protected String getEntitySuffix() {
		return UserEntity.class.getAnnotation(Table.class).name();
	}
}
