package com.gatewayservice.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserVerificationHandler 테스트")
class UserVerificationHandlerTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private UserVerificationHandler userVerificationHandler;

    private static final String TEST_TOKEN = "test.jwt.token";
    private static final String TEST_IP = "192.168.1.1";
    private static final long EXPIRATION = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userVerificationHandler, "expiration", EXPIRATION);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("토큰과 IP를 Redis에 저장")
    void addTokenAndIp_ShouldStoreInRedis() {
        // given
        String expectedKey = "request:ip:" + TEST_IP;

        // when
        userVerificationHandler.addTokenAndIp(TEST_TOKEN, TEST_IP);

        // then
        verify(valueOperations, times(1))
                .set(eq(expectedKey), eq(TEST_TOKEN), eq(EXPIRATION), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("유효한 토큰 검증 성공")
    void validateToken_WithValidToken_ShouldReturnFalse() {
        // given
        String key = "request:ip:" + TEST_IP;
        when(valueOperations.get(key)).thenReturn(TEST_TOKEN);

        // when
        boolean result = userVerificationHandler.validateToken(TEST_IP, TEST_TOKEN);

        // then
        assertThat(result).isFalse();
        verify(valueOperations, times(1)).get(key);
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증 실패")
    void validateToken_WithInvalidToken_ShouldReturnTrue() {
        // given
        String key = "request:ip:" + TEST_IP;
        String storedToken = "stored.jwt.token";
        String differentToken = "different.jwt.token";
        when(valueOperations.get(key)).thenReturn(storedToken);

        // when
        boolean result = userVerificationHandler.validateToken(TEST_IP, differentToken);

        // then
        assertThat(result).isTrue();
        verify(valueOperations, times(1)).get(key);
    }

    @Test
    @DisplayName("Redis에 토큰이 없는 경우 검증 실패")
    void validateToken_WithNoTokenInRedis_ShouldReturnTrue() {
        // given
        String key = "request:ip:" + TEST_IP;
        when(valueOperations.get(key)).thenReturn(null);

        // when
        boolean result = userVerificationHandler.validateToken(TEST_IP, TEST_TOKEN);

        // then
        assertThat(result).isTrue();
        verify(valueOperations, times(1)).get(key);
    }

    @Test
    		@DisplayName("다른 IP로 토큰 검증 시 실패")
    		void validateToken_WithDifferentIp_ShouldReturnTrue() {
    			// given
    			String ip2 = "192.168.1.2";
    			String key2 = "request:ip:" + ip2;
    	
    			when(valueOperations.get(key2)).thenReturn(null);
    	
    			// when
    			boolean result = userVerificationHandler.validateToken(ip2, TEST_TOKEN);
    	
    			// then
    			assertThat(result).isTrue();
    			verify(valueOperations, times(1)).get(key2);
    		}    @Test
    @DisplayName("여러 IP에 대해 각각 토큰 저장")
    void addTokenAndIp_ForMultipleIps_ShouldStoreEachSeparately() {
        // given
        String ip1 = "192.168.1.1";
        String ip2 = "192.168.1.2";
        String token1 = "token1";
        String token2 = "token2";

        // when
        userVerificationHandler.addTokenAndIp(token1, ip1);
        userVerificationHandler.addTokenAndIp(token2, ip2);

        // then
        verify(valueOperations, times(1))
                .set(eq("request:ip:" + ip1), eq(token1), eq(EXPIRATION), eq(TimeUnit.MILLISECONDS));
        verify(valueOperations, times(1))
                .set(eq("request:ip:" + ip2), eq(token2), eq(EXPIRATION), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("토큰 갱신 시 기존 토큰 덮어쓰기")
    void addTokenAndIp_SameIp_ShouldOverwriteExistingToken() {
        // given
        String oldToken = "old.token";
        String newToken = "new.token";
        String key = "request:ip:" + TEST_IP;

        // when
        userVerificationHandler.addTokenAndIp(oldToken, TEST_IP);
        userVerificationHandler.addTokenAndIp(newToken, TEST_IP);

        // then
        verify(valueOperations, times(2))
                .set(eq(key), anyString(), eq(EXPIRATION), eq(TimeUnit.MILLISECONDS));
    }
}
