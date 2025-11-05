package com.userservice.infrastructure.web.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.userservice.application.adapter.dto.UserContext;
import com.userservice.application.port.in.UserCommandUseCase;
import com.userservice.domain.model.User;
import com.userservice.infrastructure.web.UserContextMapper;
import com.userservice.infrastructure.web.dto.UserCreateRequest;
import com.userservice.infrastructure.web.dto.UserCreateResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserCommandUseCase userCommandUseCase;

	@PostMapping
	public BaseResponse<UserCreateResponse> createUser(
		@RequestBody UserCreateRequest request
	) {

		UserContext context = UserContextMapper.toContext(request);
		User user = userCommandUseCase.create(context);

		return new BaseResponse<>(new UserCreateResponse(
			user.getId(),
			user.getProfileUrl(),
			user.getNickname()
		),
			"가입 완료");
	}
}
