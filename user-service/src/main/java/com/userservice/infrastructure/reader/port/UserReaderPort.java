package com.userservice.infrastructure.reader.port;

import com.userservice.infrastructure.reader.port.dto.UserDescription;

public interface UserReaderPort {
	UserDescription getUserDescription(String id);
}
