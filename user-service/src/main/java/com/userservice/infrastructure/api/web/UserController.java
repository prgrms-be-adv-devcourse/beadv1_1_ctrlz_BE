package com.userservice.infrastructure.api.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.common.model.web.BaseResponse;
import com.userservice.application.adapter.command.SellerVerificationContext;
import com.userservice.application.adapter.dto.UserContext;
import com.userservice.application.adapter.dto.UserUpdateContext;
import com.userservice.application.port.in.SellerVerificationUseCase;
import com.userservice.application.port.in.UserCommandUseCase;
import com.userservice.domain.model.User;
import com.userservice.infrastructure.api.dto.UpdateSellerRequest;
import com.userservice.infrastructure.api.dto.UserCreateRequest;
import com.userservice.infrastructure.api.dto.UserCreateResponse;
import com.userservice.infrastructure.api.dto.UserUpdateRequest;
import com.userservice.infrastructure.api.dto.VerificationReqeust;
import com.userservice.infrastructure.api.mapper.UserContextMapper;
import com.userservice.infrastructure.reader.port.UserReaderPort;
import com.userservice.infrastructure.reader.port.dto.UserDescription;
import com.userservice.infrastructure.writer.ProfileImageClient;
import com.userservice.infrastructure.writer.dto.ImageResponse;

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
	private final SellerVerificationUseCase sellerVerificationUseCase;
	private final ProfileImageClient profileImageClient;

	@PostMapping
	public BaseResponse<UserCreateResponse> createUser(
		@RequestPart("profileImage") MultipartFile profileImage,
		@Valid @RequestPart("request") UserCreateRequest request
	) {

		ImageResponse imageResponse = (profileImage.isEmpty())
			? new ImageResponse(defaultImageUrl, null)
			: profileImageClient.uploadImage(profileImage);

		UserContext context = UserContextMapper.toContext(request, imageResponse);
		User user = userCommandUseCase.create(context);

		return new BaseResponse<>(new UserCreateResponse(
			user.getId(),
			user.getProfileImageUrl(),
			user.getNickname()
		),
			"가입 완료");
	}

	@PatchMapping("/{id}")
	public void updateUser(
		@PathVariable String id,
		@Valid @RequestBody UserUpdateRequest request
	) {
		UserUpdateContext context = UserContextMapper.toContext(request);
		userCommandUseCase.updateUser(id, context);
	}

	@GetMapping("/{id}")
	public UserDescription getUser(@PathVariable String id) {
		return userReaderPort.getUserDescription(id);
	}

	@PostMapping("/sellers/{id}")
	public BaseResponse<?> updateRoleForSeller(
		@PathVariable String id,
		@RequestBody UpdateSellerRequest request
	) {

		SellerVerificationContext sellerVerificationContext =
			SellerVerificationContext.toVerify(id, request.verificationCode());

		sellerVerificationUseCase.checkVerificationCode(sellerVerificationContext);
		userCommandUseCase.updateForSeller(id);

		return new BaseResponse<>(null, "판매자 등록이 완료됐습니다.");
	}

	@PostMapping("/sellers/verification/{id}")
	public void sendVerificationCode(
		@PathVariable String id,
		@RequestBody VerificationReqeust request
	) {
		User user = userCommandUseCase.getUser(id);

		SellerVerificationContext sellerVerificationContext =
			SellerVerificationContext.forSending(request.phoneNumber(), id, user);

		sellerVerificationUseCase.requestVerificationCode(sellerVerificationContext);
	}

	@PatchMapping("/{id}/images/{imageId}")
	public BaseResponse<String> updateProfileImage(
		@PathVariable String id,
		@PathVariable String imageId,
		@RequestParam("profileImage") MultipartFile profileImage
	) {
		ImageResponse imageResponse = profileImageClient.updateProfileImage(profileImage, imageId);
		userCommandUseCase.updateImage(id, imageResponse.imageId(), imageResponse.imageUrl());
		return new BaseResponse<>(imageResponse.imageUrl(), "프로필 이미지 교체 완료");
	}
}
