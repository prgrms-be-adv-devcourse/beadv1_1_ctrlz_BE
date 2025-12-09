package com.aiservice.application.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ChatClientConfiguration {

	@Bean
	public ChatClient openAiChatclient(ChatModel chatModel) {
		return ChatClient.builder(chatModel).build();
	}

	@Bean
	public PromptTemplate recommendationPromptTemplate() {
		return new PromptTemplate(
			new ClassPathResource("prompts/recommendation.st")
		);
	}

	@Profile("local")
	@Bean
	public ChatOptions chatOptions() {
		log.info("로컬 환경 openai embedding model 사용");

		return ChatOptions
			.builder()
			.model("llama-3.1-8b-instant")
			.temperature(0.7)
			.build();
	}

	// @Profile("local")
	// @Bean
	// public EmbeddingModel embeddingModel(OpenAiApi openAiApi) {
	// 	log.info("로컬 환경  embedding model 사용");
	//
	// 	return new OpenAiEmbeddingModel(
	// 		openAiApi,
	// 		MetadataMode.EMBED,
	// 		OpenAiEmbeddingOptions.builder()
	// 			.model("sdfsdfsdf")
	// 			.build(),
	// 		RetryUtils.DEFAULT_RETRY_TEMPLATE
	// 	);
	// }
}
