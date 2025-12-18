package com.aiservice.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class SseCommunicationServiceTest {

    @Mock
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @InjectMocks
    private SseCommunicationService sseCommunicationService;

    @DisplayName("SSE 연결 성공 및 Redis 구독 확인")
    @Test
    void test1() {
        // given
        String userId = "testUser";

        // when
        SseEmitter emitter = sseCommunicationService.connect(userId);

        // then
		assertThat(emitter).isNotNull();
        verify(redisMessageListenerContainer, times(1)).addMessageListener(any(), any(ChannelTopic.class));
    }
}
