package com.userservice.application.port.in;

import com.userservice.application.adapter.command.SellerVerificationContext;

public interface SellerVerificationUseCase {
	void requestVerificationCode(SellerVerificationContext sellerVerificationContext);
	void checkVerificationCode(SellerVerificationContext sellerVerificationContext);
}
