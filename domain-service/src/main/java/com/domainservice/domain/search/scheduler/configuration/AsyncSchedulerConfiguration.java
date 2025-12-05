package com.domainservice.domain.search.scheduler.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
public class AsyncSchedulerConfiguration implements SchedulingConfigurer {

	private static final int POOL_SIZE = 5;

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(POOL_SIZE);
		scheduler.setThreadNamePrefix("scheduler-thread-");
		scheduler.initialize();

		taskRegistrar.setTaskScheduler(scheduler);
	}
}
