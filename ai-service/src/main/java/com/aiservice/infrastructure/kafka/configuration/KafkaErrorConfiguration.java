package com.aiservice.infrastructure.kafka.configuration;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class KafkaErrorConfiguration {

    /**
     * Kafka 리스너 에러 핸들러 설정
     * - 재시도: 1초 간격, 최대 3회
     * - 실패 시 DLT으로 메시지 전송
     */
    @Bean
    public CommonErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
        // DLT로 메시지를 보내는 복구 로직
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
                (record, ex) -> {
                    log.error("메시지 처리 최종 실패. DLT로 이동 - Topic: {}, Partition: {}, Offset: {}, Error: {}",
                            record.topic(), record.partition(), record.offset(), ex.getMessage());
                    
                    // 명시적으로 DLT 토픽과 파티션 지정 (원본 파티션 유지로 순서 보장)
                    return new TopicPartition(
                        record.topic() + ".DLT", 
                        record.partition()
                    );
                });

        // 1초 간격으로 3회 재시도 (총 4회 시도)
        FixedBackOff backOff = new FixedBackOff(1000L, 3L);
        return new DefaultErrorHandler(recoverer, backOff);
    }
}
