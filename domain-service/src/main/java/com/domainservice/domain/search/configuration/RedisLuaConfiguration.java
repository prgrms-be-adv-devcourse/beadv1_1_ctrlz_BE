package com.domainservice.domain.search.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RedisLuaConfiguration {

	@Bean
	public RedisScript<Long> updateZSetWithDecayScript() {
		DefaultRedisScript<Long> script = new DefaultRedisScript<>();
		script.setLocation(new ClassPathResource("lua/update_zset_with_decay.lua"));
		script.setResultType(Long.class);
		return script;
	}
}
