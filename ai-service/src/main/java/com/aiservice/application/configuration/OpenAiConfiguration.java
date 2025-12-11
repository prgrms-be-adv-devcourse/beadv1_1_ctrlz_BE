package com.aiservice.application.configuration;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("prod")
@Configuration
public class OpenAiConfiguration {

	@Value("${spring.ai.openai.api-key}")
	private String apiKey;

	@Value("${spring.ai.openai.embedding.options.model:test}")
	private String model;

	@Value("${vectorstore.qdrant.embedding-dimensions:1536}")
	private int embeddingDimensions;

	@Bean
	public OpenAiApi openAiApi() {
		return OpenAiApi.builder()
				.apiKey(apiKey)
				.build();
	}

	@Bean
	public EmbeddingModel embeddingModel(OpenAiApi openAiApi) {
		log.info("운영 환경 openai embedding model 사용 - dimensions: {}", embeddingDimensions);
		return new OpenAiEmbeddingModel(
				openAiApi,
				MetadataMode.EMBED,
				OpenAiEmbeddingOptions.builder()
						.model("text-embedding-3-small")
						.dimensions(embeddingDimensions)
						.build(),
				RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

	@Bean
	public ChatModel chatModel(OpenAiApi openAiApi) {
		return OpenAiChatModel.builder()
				.openAiApi(openAiApi)
				.build();
	}

	@Bean
	public OpenAiChatOptions recommendationChatOptions() {
		return OpenAiChatOptions.builder()
				.model("gpt-4o-mini")
				.temperature(0.7)
				.build();
	}
}
