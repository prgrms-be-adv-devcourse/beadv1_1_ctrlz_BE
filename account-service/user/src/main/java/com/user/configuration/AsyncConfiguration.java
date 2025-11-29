package com.user.configuration;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfiguration {

	@Bean(name = "taskExecutor")
	public Executor someTaskExecutor() {
		TaskExecutorAdapter executorAdapter = new TaskExecutorAdapter(
			new VirtualThreadTaskExecutor("kafka-async-")
		);
		executorAdapter.setTaskDecorator(new CustomTaskDecorator());
		return executorAdapter;
	}
}
