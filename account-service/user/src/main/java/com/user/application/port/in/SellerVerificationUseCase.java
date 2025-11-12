package com.user.application.port.in;

import com.user.application.adapter.command.SellerVerificationContext;

public interface SellerVerificationUseCase {
	void requestVerificationCode(SellerVerificationContext sellerVerificationContext);
	void checkVerificationCode(SellerVerificationContext sellerVerificationContext);
}
