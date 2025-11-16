package com.user.application.adapter;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.common.exception.CustomException;
import com.common.exception.vo.UserExceptionCode;
import com.user.application.adapter.command.SellerVerificationContext;
import com.user.application.port.in.SellerVerificationUseCase;
import com.user.application.port.out.SellerVerificationClient;
import com.user.infrastructure.redis.vo.CacheType;
import com.user.infrastructure.sms.utils.VerificationCodeSupplier;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * - 인증번호 캐시 유효 시간: 1분
 * - 재시도 캐시 유효 시간: 2분
 * - 인증 제한 시간: 1일
 *
 * 설정: {@link com.user.infrastructure.redis}
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SmsApplication implements SellerVerificationUseCase {

	private final CacheManager cacheManager;
	private final SellerVerificationClient smsClientAdapter;

	private Cache verificationTryCache;
	private Cache verificationCodeCache;
	private Cache verificationBanCache;

	private static final int MAX_VERIFICATION_ATTEMPTS = 10;

	@PostConstruct
	public void init() {
		verificationTryCache = getCache(CacheType.VERIFICATION_TRY);
		verificationCodeCache = getCache(CacheType.VERIFICATION_CODE);
		verificationBanCache = getCache(CacheType.VERIFICATION_BAN_ONE_DAY);
	}

	@Override
	public void requestVerificationCode(SellerVerificationContext context) {
		validateNoActiveCode(context.getUserId());

		String code = VerificationCodeSupplier.generateCode();
		putToCacheSafely(verificationCodeCache, context.getUserId(), code);

		smsClientAdapter.send(context.getPhoneNumber(), code);
		log.info("인증번호 발송 성공 userId: {}, phone: {}", context.getUserId(), context.getPhoneNumber());
	}

	@Override
	public void checkVerificationCode(SellerVerificationContext context) {
		String cachedCode = getFromCacheSafely(verificationCodeCache, context.getUserId(), String.class);

		if (cachedCode == null || cachedCode.isBlank()) {
			throw new CustomException(UserExceptionCode.CODE_MISMATCH.getMessage());
		}

		if (!cachedCode.equals(context.getVerificationCode())) {
			handleIncorrectCode(context.getUserId());
			throw new CustomException(UserExceptionCode.CODE_MISMATCH.getMessage());
		}

		clearCachesAfterSuccess(context.getUserId());
		log.info("인증 성공 userId: {}", context.getUserId());
	}

	private void validateNoActiveCode(String userId) {
		try {
			String existingCode = verificationCodeCache.get(userId, String.class);
			if (existingCode != null) {
				throw new CustomException("인증 번호가 이미 전송되었습니다.");
			}
		} catch (Exception e) {
			log.warn("기존 인증번호 조회 실패 userId: {}", userId, e);
			throw e;
		}
	}

	private void handleIncorrectCode(String userId) {
		int attemptCount = incrementAttemptCount(userId);
		if (attemptCount >= MAX_VERIFICATION_ATTEMPTS) {
			banUser(userId);
			throw new CustomException(UserExceptionCode.VERIFICATION_COUNT_LIMIT.getMessage());
		}
	}

	private int incrementAttemptCount(String userId) {
		try {
			AtomicInteger counter = verificationTryCache.get(userId, AtomicInteger.class);
			if (counter == null) {
				counter = new AtomicInteger(0);
			}

			int newCount = counter.incrementAndGet();
			verificationTryCache.put(userId, counter);
			return newCount;
		} catch (Exception e) {
			log.error("인증 시도 횟수 증가 실패 userId: {}", userId, e);
			return 0;
		}
	}

	private void banUser(String userId) {
		try {
			verificationTryCache.evict(userId);
			verificationBanCache.put("ban_user", userId);
			log.warn("인증 시도 횟수 초과로 사용자 차단 userId: {}", userId);
		} catch (Exception e) {
			log.error("사용자 차단 캐시 처리 실패 userId: {}", userId, e);
		}
	}

	private void clearCachesAfterSuccess(String userId) {
		try {
			verificationTryCache.evict(userId);
			verificationCodeCache.evict(userId);
		} catch (Exception e) {
			log.warn("인증 성공 후 캐시 삭제 실패 userId: {}", userId, e);
		}
	}

	private <T> T getFromCacheSafely(Cache cache, String key, Class<T> type) {
		try {
			return cache.get(key, type);
		} catch (Exception e) {
			log.error("{} — userId: {}", e.getMessage(), key, e);
			throw new CustomException(UserExceptionCode.CODE_EXCEPTION.getMessage());
		}
	}

	private void putToCacheSafely(Cache cache, String key, Object value) {
		try {
			cache.put(key, value);
		} catch (Exception e) {
			log.error("{} — userId: {}", e.getMessage(), key, e);
			throw new CustomException(UserExceptionCode.CODE_EXCEPTION.getMessage());
		}
	}

	private Cache getCache(CacheType type) {
		Cache cache = cacheManager.getCache(type.name());
		if (cache == null) {
			log.error("캐시 초기화 실패: {}", cacheManager.getCacheNames());
			throw new IllegalStateException("캐싱이 초기화되지 않았습니다. type = " + type.name());
		}
		return cache;
	}
}
