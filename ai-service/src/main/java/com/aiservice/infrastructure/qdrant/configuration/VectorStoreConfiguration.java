// package com.aiservice.infrastructure.qdrant.configuration;
//
// import org.springframework.ai.embedding.EmbeddingModel;
// import org.springframework.ai.vectorstore.VectorStore;
// import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Profile;
//
// import io.qdrant.client.QdrantClient;
// import lombok.extern.slf4j.Slf4j;
//
// @Slf4j
// @Profile("local || me")
// @Configuration
// public class VectorStoreConfiguration {
//
// 	@Value("${vectorstore.qdrant.collection-name:test}")
// 	private String collectionName;
//
// 	@Value(("${vectorstore.qdrant.initialize-schema:true}"))
// 	private boolean initializeSchema;
//
//
// 	@Bean
// 	public VectorStore qdrantVectorStore(QdrantClient qdrantClient, EmbeddingModel embeddingModel) {
// 		log.info("Qdrant VectorStore 초기화 - Collection: {}, InitializeSchema: {}",
// 				collectionName, initializeSchema);
//
// 		return QdrantVectorStore.builder(qdrantClient, embeddingModel)
// 				.collectionName(collectionName)
// 				.initializeSchema(initializeSchema)
// 				.build();
// 	}
// }
