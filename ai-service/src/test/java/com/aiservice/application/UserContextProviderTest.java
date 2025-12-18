package com.aiservice.application;

import static org.assertj.core.api.Assertions.*;

import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aiservice.domain.model.UserBehavior;
import com.aiservice.domain.model.UserBehaviorType;
import com.aiservice.domain.model.UserContext;
import com.aiservice.domain.repository.UserBehaviorRepository;
import com.aiservice.infrastructure.feign.DomainServiceClient;
import com.aiservice.infrastructure.feign.UserInfoClient;
import com.aiservice.infrastructure.feign.dto.CartItemResponse;
import com.aiservice.infrastructure.feign.dto.UserDemographicDescription;

@ExtendWith(MockitoExtension.class)
class UserContextProviderTest {

    @Mock
    private UserBehaviorRepository userBehaviorRepository;

    @Mock
    private UserInfoClient userInfoClient;

    @Mock
    private DomainServiceClient domainServiceClient;

    @InjectMocks
    private UserContextProvider userContextProvider;

    @DisplayName("모든 정보 조회 성공 시 UserContext 정상 반환")
    @Test
    void test1() {
        // given
        String userId = "user1";

        UserBehavior searchBehavior = new UserBehavior(userId, "laptop", UserBehaviorType.SEARCH);
        UserBehavior viewBehavior = new UserBehavior(userId, "monitor", UserBehaviorType.VIEW);
        when(userBehaviorRepository.findByUserId(userId))
                .thenReturn(List.of(searchBehavior, viewBehavior));

        UserDemographicDescription userInfo = new UserDemographicDescription(25, "M");
        when(userInfoClient.getRecommendationInfo(userId)).thenReturn(userInfo);

        CartItemResponse cartItem = new CartItemResponse("cart1", "mouse title", "mouse",
                java.math.BigDecimal.valueOf(50000), true);
        when(domainServiceClient.getRecentCartItems(userId)).thenReturn(List.of(cartItem));

        // when
        UserContext result = userContextProvider.getUserContext(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.age()).isEqualTo(25);
        assertThat(result.gender()).isEqualTo("M");
        assertThat(result.searchKeywords()).containsExactly("laptop");
        assertThat(result.viewedTitle()).containsExactly("monitor");
        assertThat(result.cartProductNames()).containsExactly("mouse");
    }

    @DisplayName("일부 정보 조회 실패(예외) 시 기본값 처리")
    @Test
    void test2() {
        // given
        String userId = "user2";

        when(userBehaviorRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(userInfoClient.getRecommendationInfo(userId)).thenThrow(new RuntimeException("API Error"));
        when(domainServiceClient.getRecentCartItems(userId)).thenReturn(Collections.emptyList());

        // when
        UserContext result = userContextProvider.getUserContext(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.age()).isEqualTo(0); //기본값
        assertThat(result.gender()).isNull(); // 기본값
        assertThat(result.searchKeywords()).isEmpty();
    }

    @DisplayName("타임아웃 발생 시 기본값 처리 확인")
    @Test
    void test3() {
        // given
        String userId = "user3";


        when(userBehaviorRepository.findByUserId(userId)).thenThrow(new RuntimeException("Simulated Timeout"));
        when(userInfoClient.getRecommendationInfo(userId)).thenReturn(new UserDemographicDescription(30, "F"));
        when(domainServiceClient.getRecentCartItems(userId)).thenReturn(Collections.emptyList());

        // when
        UserContext result = userContextProvider.getUserContext(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.searchKeywords()).isEmpty(); // 조회 실패시 빈값
        assertThat(result.age()).isEqualTo(30);
    }

    @DisplayName("전체 실패 시 빈 Context 반환")
    @Test
    void test4() {
        // given
        String userId = "user4";

        when(userBehaviorRepository.findByUserId(userId)).thenThrow(new RuntimeException("test"));
        when(userInfoClient.getRecommendationInfo(userId)).thenThrow(new RuntimeException("test"));
        when(domainServiceClient.getRecentCartItems(userId)).thenThrow(new RuntimeException("test"));

        // when
        UserContext result = userContextProvider.getUserContext(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.searchKeywords()).isEmpty();
        assertThat(result.cartProductNames()).isEmpty();
        assertThat(result.viewedTitle()).isEmpty();
        assertThat(result.age()).isEqualTo(0);
        assertThat(result.gender()).isNull();
    }
}
