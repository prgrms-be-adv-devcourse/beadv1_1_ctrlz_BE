package com.user.domain.model;

import java.time.LocalDateTime;
import java.util.List;

import com.common.model.persistence.BaseEntity.DeleteStatus;
import com.user.domain.vo.Address;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(of = "id")
@Getter
public class User {

	private String id;
	private String name;
	private String email;
	private String password;
	private String nickname;
	private List<com.user.domain.vo.UserRole> roles;
	private String profileImageUrl;
	private Address address;
	private String phoneNumber;
	private String oauthId;
	private String imageId;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private DeleteStatus deleteStatus;

	@Builder
	public User(
		String id,
		String name,
		String email,
		String password,
		String nickname,
		List<com.user.domain.vo.UserRole> roles,
		String profileImageUrl,
		Address address,
		String phoneNumber,
		String oauthId, String imageId,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		DeleteStatus deleteStatus
	) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.roles = roles;
		this.profileImageUrl = profileImageUrl;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.oauthId = oauthId;
		this.imageId = imageId;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deleteStatus = deleteStatus;
	}

	public List<String> getRolesToString() {
		return roles.stream().map(Enum::name).toList();
	}

	public void updateAddress(Address updatedAddress) {
		this.address = updatedAddress;
	}

	public void updatePhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}
}
