package com.domainservice.common.model.user;

import java.util.List;

public record UserResponse(
	String name,
	String nickname,
	String phoneNumber,
	String zipCode,
	String state,
	String street,
	String city,
	String details,
	String email,
	List<String> roles,
	String profileImageUrl,
	String imageId
) {
}
