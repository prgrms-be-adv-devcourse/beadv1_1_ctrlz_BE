package com.userservice.application.port.out;

public interface SellerVerificationClient {
	void send(String phoneNumber, String verificationCode);
}
