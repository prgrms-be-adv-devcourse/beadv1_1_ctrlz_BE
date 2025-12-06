package com.aiservice.application;

import java.io.IOException;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.aiservice.domain.vo.RecommandationStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationSseService {

    private final RedisMessageListenerContainer redisMessageListenerContainer;

    public SseEmitter connect(String userId) {
        SseEmitter emitter = new SseEmitter(30000L);
        String channelName = "recommendation:" + userId;

        log.info("SSE 연결 시작, 채널: {}", channelName);

        MessageListener messageListener = createMessageListener(userId, emitter);

        //redis pub/sub 구독 시작
        redisMessageListenerContainer.addMessageListener(messageListener, new ChannelTopic(channelName));

        // 클라이언트 연결 종료/타임아웃 시 구독 해지
        emitter.onCompletion(() -> {
            log.info("SSE 연결 종료 (완료), 채널: {}", channelName);
            removeListener(messageListener);
        });

        emitter.onTimeout(() -> {
            log.info("SSE 연결 시간 초과, 채널: {}", channelName);
            removeListener(messageListener);
        });

        emitter.onError((e) -> {
            log.error("SSE 연결 에러 발생, 채널: {}", channelName, e);
            removeListener(messageListener);
        });

        return emitter;
    }

    private MessageListener createMessageListener(String userId, SseEmitter emitter) {
        return (Message message, byte[] pattern) -> {
            try {
                String body = new String(message.getBody());
                emitter.send(SseEmitter.event().data(body));

                // 제한 도달 메시지면 연결 종료
                if (body.contains(RecommandationStatus.LIMIT_REACHED.name())) {
                    log.info("추천 제한 도달 메시지 수신, 사용자 SSE 종료: {}", userId);
                    emitter.complete();
                }
            } catch (IOException e) {
                log.error("SSE 메시지 전송 실패", e);
            }
        };
    }

    private void removeListener(MessageListener listener) {
        redisMessageListenerContainer.removeMessageListener(listener);
    }
}
