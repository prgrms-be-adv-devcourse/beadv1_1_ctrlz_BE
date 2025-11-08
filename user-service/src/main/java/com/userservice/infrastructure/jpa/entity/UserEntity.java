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

	private String profileImageUrl;

	@Convert(converter = UserInformationConverter.class)
	@Column(nullable = false)
	private String phoneNumber;

	@Embedded
	private EmbeddedAddress address;

	private String imageId;

	@Builder
	public UserEntity(
		String name,
		String email,
		String password,
		String profileImageUrl,
		String phoneNumber,
		EmbeddedAddress address,
		String oauthId,
		String nickname,
		String imageId
	) {
		this.oauthId = oauthId;
		this.name = name;
		this.email = email;
		this.password = password;
		this.profileImageUrl = profileImageUrl;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.nickname = nickname;
		this.imageId = imageId;

		this.roles.add(UserRole.USER);
	}

	@Override
	protected String getEntitySuffix() {
		return UserEntity.class.getAnnotation(Table.class).name();
	}

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}

	public void updateAddress(EmbeddedAddress address) {
		this.address = address;
	}

	public void updatePhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void changeProfileImage(String imageId, String profileImageUrl) {
		this.imageId = imageId;
		this.profileImageUrl = profileImageUrl;
	}
}
