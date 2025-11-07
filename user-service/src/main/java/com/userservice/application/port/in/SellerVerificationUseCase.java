package com.userservice.application.port.in;

public interface SellerVerificationUseCase {
	void sendVerificationCode(String phoneNumber);
}
