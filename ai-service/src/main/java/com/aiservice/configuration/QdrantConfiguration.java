package com.aiservice.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import lombok.extern.slf4j.Slf4j;

/**
 * Qdrant 네이티브 클라이언트 설정
 * Spring AI VectorStore와 별도로 Native Qdrant Client를 사용하여
 * Hybrid Search (Dense + Sparse) 기능을 구현하기 위한 설정
 */
@Slf4j
@Configuration
public class QdrantConfiguration {

    @Value("${spring.ai.vectorstore.qdrant.host:localhost}")
    private String qdrantHost;

    @Value("${spring.ai.vectorstore.qdrant.port:6334}")
    private int qdrantPort;

    @Value("${spring.ai.vectorstore.qdrant.use-tls:false}")
    private boolean useTls;

    @Value("${spring.ai.vectorstore.qdrant.api-key:}")
    private String apiKey;

    /**
     * Qdrant Native Client Bean
     * Hybrid Search를 위해 직접 gRPC API를 사용할 수 있도록 제공
     */
    @Bean
    public QdrantClient qdrantClient() {
        log.info("Qdrant Native Client 생성: host={}, port={}, tls={}", qdrantHost, qdrantPort, useTls);

        QdrantGrpcClient.Builder grpcClientBuilder = QdrantGrpcClient.newBuilder(
                qdrantHost,
                qdrantPort,
                useTls);

        // API Key가 있는 경우 설정
        if (apiKey != null && !apiKey.isBlank()) {
            grpcClientBuilder.withApiKey(apiKey);
        }

        return new QdrantClient(grpcClientBuilder.build());
    }
}
