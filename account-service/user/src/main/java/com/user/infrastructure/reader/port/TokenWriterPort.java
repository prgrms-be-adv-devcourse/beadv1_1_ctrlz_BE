package com.user.infrastructure.reader.port;

import com.user.infrastructure.reader.port.dto.TokenResponse;

public interface TokenWriterPort {
	TokenResponse issueToken(String userId);
}
