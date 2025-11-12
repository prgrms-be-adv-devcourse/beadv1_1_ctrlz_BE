package com.user.infrastructure.redis.configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.user.infrastructure.redis.vo.CacheType;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import redis.embedded.RedisServer;
import redis.embedded.core.RedisServerBuilder;

@Slf4j
@Profile("test || local")
@EnableCaching
@Configuration
public class EmbeddedRedisConfiguration {

	private final String host = "localhost";
	private int port;
	private RedisServer redisServer;

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

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
			.serializeKeysWith(
				RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
			)
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
			);

		Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

		cacheConfigurations.put(
			CacheType.VERIFICATION_TRY.name(),
			defaultConfig.entryTtl(Duration.ofMinutes(2))
		);

		cacheConfigurations.put(
			CacheType.VERIFICATION_CODE.name(),
			defaultConfig.entryTtl(Duration.ofMinutes(1))
		);

		cacheConfigurations.put(
			CacheType.VERIFICATION_BAN_ONE_DAY.name(),
			defaultConfig.entryTtl(Duration.ofDays(1))
		);

		return RedisCacheManager.builder(connectionFactory)
			.cacheDefaults(defaultConfig)
			.withInitialCacheConfigurations(cacheConfigurations)
			.build();
	}

	@PostConstruct
	public void startRedis() {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("win")) {
			log.warn("Embedded Redis is not supported on Windows. Skipping Redis startup.");
			return;
		}
		try {
			int defaultRedisPort = 6380;
			try {
				port = isRedisRunning() ? findAvailablePort() : defaultRedisPort;
			} catch (Exception e) {
				log.warn(
					"Failed to check if Redis is running using system commands. Falling back to socket check. Error: {}",
					e.getMessage());
				port = isPortInUse(defaultRedisPort) ? findAvailablePortUsingSocket() : defaultRedisPort;
			}

			try {
				redisServer = new RedisServerBuilder()
					.port(port)
					.setting("daemonize no")
					.setting("appendonly no")
					.setting("save \"\"")
					.setting("dbfilename \"\"")
					.setting("stop-writes-on-bgsave-error no")
					.build();

				redisServer.start();
				log.info("Embedded Redis started on port {}", port);
			} catch (Exception e) {
				log.error(" Failed to start embedded Redis server. Tests will continue without Redis. Error: {}",
					e.getMessage());
			}
		} catch (Exception e) {
			log.error("Error during Redis server initialization: {}", e.getMessage());
		}
	}

	@PreDestroy
	public void stopRedis() {
		try {
			if (redisServer != null) {
				redisServer.stop();
				log.info("Embedded Redis stopped");
			}
		} catch (Exception e) {
			log.error("Error stopping Redis server: {}", e.getMessage());
		}
	}

	public int findAvailablePort() throws IOException {
		for (int port = 10000; port <= 65535; port++) {
			try {
				Process process = executeGrepProcessCommand(port);
				if (!isRunning(process)) {
					return port;
				}
			} catch (Exception e) {
				log.warn("Error checking port {}: {}", port, e.getMessage());
			}
		}
		return findAvailablePortUsingSocket();
	}

	private int findAvailablePortUsingSocket() {
		for (int port = 10000; port <= 65535; port++) {
			if (!isPortInUse(port)) {
				return port;
			}
		}
		throw new IllegalArgumentException("Not Found Available port: 10000 ~ 65535");
	}

	private boolean isPortInUse(int port) {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			return false;
		} catch (IOException e) {
			return true;
		}
	}

	private boolean isRedisRunning() throws IOException {
		try {
			return isRunning(executeGrepProcessCommand(port));
		} catch (Exception e) {
			log.warn("Error checking if Redis is running: {}", e.getMessage());
			return false;
		}
	}

	private Process executeGrepProcessCommand(int port) throws IOException {
		try {
			String command = String.format("netstat -nat | grep LISTEN|grep %d", port);
			String[] shell = {"/bin/sh", "-c", command};
			return Runtime.getRuntime().exec(shell);
		} catch (Exception e) {
			log.warn("Failed to execute grep command: {}", e.getMessage());
			throw e;
		}
	}

	private boolean isRunning(Process process) {
		String line;
		StringBuilder pidInfo = new StringBuilder();

		try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			while ((line = input.readLine()) != null) {
				pidInfo.append(line);
			}
		} catch (Exception e) {
			log.warn("Error reading process output: {}", e.getMessage());
			return false;
		}

		return StringUtils.hasLength(pidInfo.toString());
	}
}
