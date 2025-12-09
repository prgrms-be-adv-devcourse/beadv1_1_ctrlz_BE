package com.domainservice.domain.order.event.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration(proxyBeanMethods = false)
public class KafkaTopicConfiguration {

    @Value("order-command")
    private String orderCommandTopic;

    @Value("1")
    private int topicPartitions;

    @Value("1")
    private int topicReplications;

    @Bean
    public NewTopic updateOrderStatusPaymentCompleted() {
        return TopicBuilder.name(orderCommandTopic)
            .partitions(topicPartitions)
            .replicas(topicReplications)
            .build();
    }

    @Bean
    public NewTopic updateOrderStatusRefundAfterPayment() {
        return TopicBuilder.name(orderCommandTopic)
            .partitions(topicPartitions)
            .replicas(topicReplications)
            .build();
    }
}
