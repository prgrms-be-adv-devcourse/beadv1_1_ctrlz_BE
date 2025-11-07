package com.userservice.infrastructure.sms.adapter;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.common.exception.CustomException;
import com.common.exception.vo.UserExceptionCode;
import com.userservice.infrastructure.cache.vo.CacheType;
import com.userservice.infrastructure.sms.utils.VerificationCodeSupplier;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * 캐시 유효 시간은 1분으로 설정하였습니다. {@link com.userservice.infrastructure.cache.Configuration.CacheConfiguration}
 */
@RequiredArgsConstructor
@Service
public class SmsService {

	private final CacheManager cacheManager;
	private final SmsClient smsClient;
	private final AtomicInteger counter = new AtomicInteger(0);
	private Cache verificationTryCache;

	@PostConstruct
	public void init() {
		verificationTryCache = cacheManager.getCache(CacheType.VERIFICATION_TRY.name());
	}

	public void sendSms(String phoneNumber) {
		applyVerificationCount(phoneNumber);
		smsClient.send(phoneNumber, VerificationCodeSupplier.generateCode());
	}



	// 인증 횟수 추가/체크
	void applyVerificationCount(String phoneNumber) {

		if (verificationTryCache == null) {
			throw new RuntimeException("verificationTryCache 캐시 생성 오류");
		}

		AtomicInteger count = verificationTryCache.get(phoneNumber, AtomicInteger.class);
		if (count == null) {
			count = new AtomicInteger(0);
			verificationTryCache.put(phoneNumber, count);
		}

		int current = count.incrementAndGet();
		if (current >= 5) {
			verificationTryCache.evict(phoneNumber);
			throw new CustomException(UserExceptionCode.VERIFICATION_COUNT_LIMIT.getMessage());
		}
	}
}
