package com.userservice.application.utils;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EncryptEncoder {

	private final PasswordEncoder passwordEncoder;

	public String encode(String target) {
		return passwordEncoder.encode(target);
	}
}
