package com.accountapplication.springDoc;

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

	@Value("${openapi.service.url}")
	private String gatewayUrl;

	@Value("${server.port:8080}")
	private String localPort;

	@Bean
	public OpenAPI customOpenAPI() throws IOException {

		ClassPathResource resource = new ClassPathResource("docs/responseDescription.txt");
		String description = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

		return new OpenAPI()
			.servers(
				List.of(
					new Server().url(gatewayUrl).description("Gateway Server"),
					new Server().url("http://localhost:" + localPort).description("local Server")
				)
			)
			.info(new Info()
				.title("연근마켓 - Domain Service API")
				.version("v1.0.0")
				.description(description)
			);
	}
}