package com.user.infrastructure.reader.port;

import com.user.infrastructure.reader.port.dto.TokenResponse;

public interface TokenWriterPort {
	TokenResponse issueUserRoleToken(String userId);
	TokenResponse issueSellerRoleToken(String userId);
}
