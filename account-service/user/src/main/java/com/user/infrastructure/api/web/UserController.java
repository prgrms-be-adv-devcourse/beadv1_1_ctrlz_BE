package com.user.infrastructure.api.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.common.model.web.BaseResponse;
import com.user.application.adapter.command.SellerVerificationContext;
import com.user.application.adapter.dto.UserContext;
import com.user.application.adapter.dto.UserUpdateContext;
import com.user.application.port.in.SellerVerificationUseCase;
import com.user.application.port.in.UserCommandUseCase;
import com.user.domain.model.User;
import com.user.infrastructure.api.dto.UpdateSellerRequest;
import com.user.infrastructure.api.dto.UserCreateRequest;
import com.user.infrastructure.api.dto.UserCreateResponse;
import com.user.infrastructure.api.dto.UserUpdateRequest;
import com.user.infrastructure.api.dto.VerificationReqeust;
import com.user.infrastructure.api.mapper.UserContextMapper;
import com.user.infrastructure.feign.ProfileImageClient;
import com.user.infrastructure.feign.dto.ImageResponse;
import com.user.infrastructure.reader.port.TokenWriterPort;
import com.user.infrastructure.reader.port.UserReaderPort;
import com.user.infrastructure.reader.port.dto.TokenResponse;
import com.user.infrastructure.reader.port.dto.UserDescription;

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
	private final TokenWriterPort tokenWriterPort;
	private final ProfileImageClient profileImageClient;

	@PostMapping
	public ResponseEntity<BaseResponse<UserCreateResponse>> createUser(
		@Valid @RequestBody UserCreateRequest request
	) {

		UserContext context = UserContextMapper.toContext(request, defaultImageUrl);
		UserContext savedUserContext = userCommandUseCase.create(context);

		MultiValueMap<String, String> headers = addTokenInHeader(savedUserContext);
		BaseResponse<UserCreateResponse> responseBody = addUserInBody(savedUserContext);

		return new ResponseEntity<>(responseBody, headers, HttpStatus.CREATED);
	}

	@PatchMapping("/{id}")
	public void updateUser(
		@PathVariable("id") String id,
		@Valid @RequestBody UserUpdateRequest request
	) {
		UserUpdateContext context = UserContextMapper.toContext(request);
		userCommandUseCase.updateUser(id, context);
	}

	@GetMapping("/{id}")
	public UserDescription getUser(@PathVariable("id") String id) {
		return userReaderPort.getUserDescription(id);
	}

	@PostMapping("/sellers/{id}")
	public BaseResponse<Void> updateRoleForSeller(
		@PathVariable("id") String id,
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
		@PathVariable("id") String id,
		@RequestBody VerificationReqeust request
	) {
		User user = userCommandUseCase.getUser(id);

		SellerVerificationContext sellerVerificationContext =
			SellerVerificationContext.forSending(request.phoneNumber(), id, user);

		sellerVerificationUseCase.requestVerificationCode(sellerVerificationContext);
	}

	@PatchMapping("/{id}/images/{imageId}")
	public BaseResponse<String> updateProfileImage(
		@PathVariable("id") String id,
		@PathVariable("imageId") String imageId,
		@RequestParam("profileImage") MultipartFile profileImage
	) {
		ImageResponse imageResponse = profileImageClient.updateProfileImage(profileImage, imageId);
		userCommandUseCase.updateImage(id, imageResponse.imageId(), imageResponse.imageUrl());
		return new BaseResponse<>(imageResponse.imageUrl(), "프로필 이미지 교체 완료");
	}

	@DeleteMapping("/{id}")
	public void deleteUser(
		@PathVariable("id") String id
	) {
		userCommandUseCase.delete(id);
	}

	private MultiValueMap<String, String> addTokenInHeader(UserContext savedUserContext) {
		TokenResponse tokenResponse = tokenWriterPort.issueToken(savedUserContext.userId());

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Set-Cookie", tokenResponse.accessToken().toString());
		headers.add("Set-Cookie", tokenResponse.refreshToken().toString());
		return headers;
	}

	private static BaseResponse<UserCreateResponse> addUserInBody(UserContext savedUserContext) {
		return new BaseResponse<>(new UserCreateResponse(
			savedUserContext.userId(),
			savedUserContext.profileImageUrl(),
			savedUserContext.nickname()
		),
			"가입 완료");
	}
}
