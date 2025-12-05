package com.aiservice.infrastructure.kafka;

import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.aiservice.application.ProductRecommendationService;
import com.aiservice.domain.event.ProductPostSearchedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile("prod")
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSearchEventListener {

    private final ProductRecommendationService productRecommendationService;

    @KafkaListener(topics = "${custom.product-search.topic.event}", groupId = "${spring.kafka.consumer.group-id}")
    public void handler(ProductPostSearchedEvent event) {
        log.info("ProductSearchEvent 수신 - 사용자: {}, 쿼리: {}", event.userId(), event.query());
        try {
            productRecommendationService.recommendProductsByQuery(event.userId(), event.query());
        } catch (Exception e) {
            log.error("ProductSearchEvent 처리 중 오류 발생", e);
        }
    }
}
