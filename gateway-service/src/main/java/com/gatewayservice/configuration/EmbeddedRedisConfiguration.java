package com.gatewayservice.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.embedded.RedisServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;

@Slf4j
@Profile("test || local")
@EnableCaching
@Configuration
public class EmbeddedRedisConfiguration {

	@Value("${spring.data.redis.host:localhost}")
	private String host;

	@Value("${spring.data.redis.port:0}") // 0이면 자동 할당
	private int port;

	private RedisServer redisServer;

	private static final String OS = System.getProperty("os.name").toLowerCase();
	private static final boolean IS_WINDOWS = OS.contains("win");

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
		return new LettuceConnectionFactory(config);
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(serializer);
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(serializer);
		template.afterPropertiesSet();
		return template;
	}

	@PostConstruct
	public void startRedis() {
		try {
			// 포트가 0이면 자동 할당, 아니면 지정된 포트 사용
			if (port == 0) {
				port = findAvailablePort();
			} else if (isPortInUse(port)) {
				log.info("Port {} is already in use. Finding another port...", port);
				port = findAvailablePort();
			}

			redisServer = new RedisServer(port);

			redisServer.start();
			log.info("Embedded Redis started successfully on port {}", port);

		} catch (Exception e) {
			log.error("Failed to start embedded Redis on port {}. Tests will run without Redis cache. Error: {}", port,
					e.getMessage());
			// Redis 없이도 테스트 진행 가능하도록 예외 던지지 않음
		}
	}

	@PreDestroy
	public void stopRedis() {
		if (redisServer != null && redisServer.isActive()) {
			try {
				redisServer.stop();
				log.info("Embedded Redis stopped on port {}", port);
			} catch (Exception e) {
				log.error("Error stopping embedded Redis: {}", e.getMessage());
			}
		}
	}

	private int findAvailablePort() {
		for (int p = 10000; p <= 65535; p++) {
			if (!isPortInUse(p)) {
				return p;
			}
		}
		throw new IllegalStateException("No available port found in range 10000-65535");
	}

	private boolean isPortInUse(int port) {
		if (IS_WINDOWS) {
			return isPortInUseOnWindows(port);
		} else {
			return isPortInUseOnUnix(port);
		}
	}

	// Windows용: netstat -ano | findstr :포트
	private boolean isPortInUseOnWindows(int port) {
		try {
			Process process = Runtime.getRuntime().exec("netstat -ano | findstr :" + port);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				return reader.lines().anyMatch(line -> line.contains("LISTENING"));
			}
		} catch (Exception e) {
			log.debug("Fallback to socket check due to netstat failure: {}", e.getMessage());
			return isPortInUseBySocket(port);
		}
	}

	// Unix/macOS용
	private boolean isPortInUseOnUnix(int port) {
		try {
			String[] command = { "/bin/sh", "-c", "lsof -i :" + port + " | grep LISTEN" };
			Process process = Runtime.getRuntime().exec(command);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				return reader.readLine() != null;
			}
		} catch (Exception e) {
			log.debug("Fallback to socket check due to lsof/netstat failure: {}", e.getMessage());
			return isPortInUseBySocket(port);
		}
	}

	// 가장 확실한 방법: ServerSocket으로 직접 확인
	private boolean isPortInUseBySocket(int port) {
		try (ServerSocket socket = new ServerSocket(port)) {
			socket.setReuseAddress(true);
			return false;
		} catch (IOException e) {
			return true;
		}
	}
}