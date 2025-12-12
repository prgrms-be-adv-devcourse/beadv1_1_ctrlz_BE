package com.aiservice.application;

import com.aiservice.domain.model.UserContext;

public interface UserContextService {
    UserContext getUserContext(String userId);
}
