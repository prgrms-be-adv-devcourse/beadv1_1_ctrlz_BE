package com.userservice.infrastructure.api.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.common.asset.image.infrastructure.ProfileImageUploadClient;
import com.common.model.web.BaseResponse;
import com.userservice.application.adapter.dto.UserContext;
import com.userservice.application.port.in.UserCommandUseCase;
import com.userservice.domain.model.User;
import com.userservice.infrastructure.api.dto.UserCreateRequest;
import com.userservice.infrastructure.api.dto.UserCreateResponse;
import com.userservice.infrastructure.api.mapper.UserContextMapper;
import com.userservice.infrastructure.reader.port.UserReaderPort;
import com.userservice.infrastructure.reader.port.dto.UserDescription;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

	@Value("${custom.image.default}")
	private String defaultImageUrl;

	private final UserReaderPort userReaderPort;
	private final UserCommandUseCase userCommandUseCase;
	private final ProfileImageUploadClient profileImageUploadClient;

	@PostMapping
	public BaseResponse<UserCreateResponse> createUser(
		@RequestPart("profileImage") MultipartFile profileImage,
		@Valid @RequestPart("request") UserCreateRequest request
	) {

		String imageUrl = (profileImage.isEmpty())
			? defaultImageUrl
			: profileImageUploadClient.uploadImage(profileImage).profileUrl();

		UserContext context = UserContextMapper.toContext(request, imageUrl);
		User user = userCommandUseCase.create(context);

		return new BaseResponse<>(new UserCreateResponse(
			user.getId(),
			user.getProfileUrl(),
			user.getNickname()
		),
			"가입 완료");
	}

	@GetMapping("/{id}")
	public UserDescription getUser(@PathVariable String id) {
		return userReaderPort.getUserDescription(id);
	}
}
