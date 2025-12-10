package com.paymentservice.config;

import java.io.IOException;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import okhttp3.mockwebserver.MockWebServer;

@TestConfiguration
public class MockWebServerConfig {

    private static final MockWebServer mockWebServer = new MockWebServer();

    @Bean
    public MockWebServer mockWebServer() throws IOException {
        mockWebServer.start();
        return mockWebServer;
    }
}