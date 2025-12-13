package com.domainservice.common.configuration.springDoc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SpringDocConfiguration {

	@Bean
	public OpenAPI customOpenAPI(
		@Value("${openapi.service.url}") String gatewayUrl
	) throws IOException {

		ClassPathResource resource = new ClassPathResource("docs/responseDescription.txt");
		String description = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

		return new OpenAPI()
			.servers(List.of(
				new Server().url(gatewayUrl).description("Gateway Server"),
				new Server().url("http://localhost:8081").description("Direct Access")
			))
			.servers(List.of(new Server().url(gatewayUrl).description("Gateway Server")))
			.info(new Info()
				.title("연근마켓 - Domain Service API")
				.version("v1.0.0")
				.description(description)
			);
	}

}
