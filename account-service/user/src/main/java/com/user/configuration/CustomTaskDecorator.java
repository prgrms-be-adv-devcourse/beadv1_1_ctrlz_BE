package com.user.configuration;

import java.util.Map;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

public class CustomTaskDecorator implements TaskDecorator {

	@Override
	public Runnable decorate(Runnable task) {
		Map<String, String> callerThreadContext = MDC.getCopyOfContextMap();
		return () -> {
			if(callerThreadContext != null){
				MDC.setContextMap(callerThreadContext);
			}
			task.run();
		};
	}
}
