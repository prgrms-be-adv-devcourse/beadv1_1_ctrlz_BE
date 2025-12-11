package com.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class TimeTraceAspect {

	@Around("@annotation(com.common.annotation.TimeTrace)")
	public Object traceExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

		long start = System.currentTimeMillis();
		String methodName = joinPoint.getSignature().toShortString();

		try {
			return joinPoint.proceed();
		} finally {
			long end = System.currentTimeMillis();
			long duration = end - start;

			log.info("[TimeTrace] {} executed in {} ms", methodName, duration);
		}
	}
}
