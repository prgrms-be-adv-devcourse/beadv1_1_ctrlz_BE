package com.user.infrastructure.reader.port;

import com.user.infrastructure.reader.port.dto.UserDescription;

public interface UserReaderPort {
	UserDescription getUserDescription(String id);
}
