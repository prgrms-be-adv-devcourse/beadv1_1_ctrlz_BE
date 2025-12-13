package com.domainservice.common.configuration.springDoc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SpringDocConfiguration {

	@Bean
	public GroupedOpenApi groupController() {
		return GroupedOpenApi.builder()
			.group("controller")
			.pathsToMatch("/api/**")
			.build();
	}

	@Bean
	public OpenAPI customOpenAPI() throws IOException {

		ClassPathResource resource = new ClassPathResource("docs/responseDescription.txt");
		String description = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

		return new OpenAPI()
			.info(new Info()
				.title("연근마켓 이커머스 플랫폼 API 서버")
				.version("v1.0.0")
				.description(description)
			);

	}
}
