package com.aiservice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.aiservice.domain.model.UserContext;
import com.aiservice.domain.repository.UserBehaviorRepository;
import com.aiservice.infrastructure.configuration.UserContextCacheConfiguration;
import com.aiservice.infrastructure.feign.DomainServiceClient;
import com.aiservice.infrastructure.feign.UserInfoClient;
import com.aiservice.infrastructure.feign.dto.UserDemographicDescription;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { UserContextProvider.class, UserContextCacheConfiguration.class })
@EnableCaching
class UserContextCacheIntegrationTest {

    @Autowired
    private UserContextService userContextProvider;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private UserBehaviorRepository userBehaviorRepository;

    @MockBean
    private UserInfoClient userInfoClient;

    @MockBean
    private DomainServiceClient domainServiceClient;

    @DisplayName("캐시 적용 확인 - 두 번째 호출은 메서드 실행 없이 캐시된 값 반환")
    @Test
    void testCacheBehavior() {
        // given
        String userId = "cacheUser";

        when(userBehaviorRepository.findByUserId(userId)).thenReturn(List.of());
        when(userInfoClient.getRecommendationInfo(userId)).thenReturn(new UserDemographicDescription(20, "M"));
        when(domainServiceClient.getRecentCartItems(userId)).thenReturn(List.of());

        // when
        UserContext result1 = userContextProvider.getUserContext(userId);
        UserContext result2 = userContextProvider.getUserContext(userId);

        // then
        assertThat(result1).isEqualTo(result2);

        verify(userInfoClient, times(1)).getRecommendationInfo(userId);

        assertThat(cacheManager.getCache("userContext")).isNotNull();
    }
}
