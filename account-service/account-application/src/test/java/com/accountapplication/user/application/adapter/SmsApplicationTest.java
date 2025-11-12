package com.accountapplication.user.application.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.common.exception.CustomException;
import com.user.application.adapter.SmsApplication;
import com.user.application.adapter.command.SellerVerificationContext;
import com.user.infrastructure.redis.vo.CacheType;
import com.user.infrastructure.sms.adapter.SmsClientAdapter;


@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SmsApplicationTest {

    @Autowired
    private SmsApplication smsApplication;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private SmsClientAdapter smsClientAdapter;

    private Cache getCache(CacheType cacheType) {
        return cacheManager.getCache(cacheType.name());
    }

    @AfterEach
    void tearDown() {
        for (String cacheName : cacheManager.getCacheNames()) {
            cacheManager.getCache(cacheName).clear();
        }
    }

    @DisplayName("인증 코드 요청")
    @Nested
    class RequestVerificationCode {

        @DisplayName("새로운 인증 코드를 요청하면 SMS가 발송된다")
        @Test
        void test1() {
            // given
            SellerVerificationContext context = mock(SellerVerificationContext.class);
            given(context.getUserId()).willReturn("user123");
            given(context.getPhoneNumber()).willReturn("01012345678");

            // when
            smsApplication.requestVerificationCode(context);

            // then
            assertThat(getCache(CacheType.VERIFICATION_CODE).get("user123")).isNotNull();
            then(smsClientAdapter).should().send(eq("01012345678"), anyString());
        }
    }

    @DisplayName("인증 코드 확인")
    @Nested
    class CheckVerificationCode {

        @DisplayName("인증 코드로 검증에 성공한다")
        @Test
        void test1() {
            // given
            SellerVerificationContext context = mock(SellerVerificationContext.class);
            given(context.getUserId()).willReturn("user123");
            given(context.getVerificationCode()).willReturn("123456");
            getCache(CacheType.VERIFICATION_CODE).put("user123", "123456");

            // when then
            assertThatCode(() -> smsApplication.checkVerificationCode(context))
                .doesNotThrowAnyException();
        }

        @DisplayName("인증 코드가 존재하지 않으면 예외가 발생한다")
        @Test
        void test2() {
            // given
            SellerVerificationContext context = mock(SellerVerificationContext.class);
            given(context.getUserId()).willReturn("user123");

            // when then
            assertThatThrownBy(() -> smsApplication.checkVerificationCode(context))
                .isInstanceOf(CustomException.class);
        }

        @DisplayName("인증 코드가 일치하지 않으면 예외가 발생하고 카운트가 증가한다")
        @Test
        void test3() {
            // given
            SellerVerificationContext context = mock(SellerVerificationContext.class);
            given(context.getUserId()).willReturn("user123");
            given(context.getVerificationCode()).willReturn("123456");
            getCache(CacheType.VERIFICATION_CODE).put("user123", "654321");

            // when then
            assertThatThrownBy(() -> smsApplication.checkVerificationCode(context))
                .isInstanceOf(CustomException.class);

            AtomicInteger count = getCache(CacheType.VERIFICATION_TRY).get("user123", AtomicInteger.class);
            assertThat(count.get()).isEqualTo(1);
        }
    }

    @DisplayName("인증 코드 요청")
    @Nested
    class AdditionalRequestVerificationCode {

        @DisplayName("이미 발송된 인증 코드가 있으면 예외가 발생한다")
        @Test
        void test1() {
            // given
            SellerVerificationContext context = mock(SellerVerificationContext.class);
            given(context.getUserId()).willReturn("user123");
            given(context.getPhoneNumber()).willReturn("01012345678");
            
            getCache(CacheType.VERIFICATION_CODE).put("user123", "code");

            // when then
            assertThatThrownBy(() -> smsApplication.requestVerificationCode(context))
                .isInstanceOf(CustomException.class);
        }

        @DisplayName("인증 코드 발송 시 캐시에 저장된다")
        @Test
        void test2() {
            // given
            SellerVerificationContext context = mock(SellerVerificationContext.class);
            given(context.getUserId()).willReturn("user456");
            given(context.getPhoneNumber()).willReturn("01098765432");

            // when
            smsApplication.requestVerificationCode(context);

            // then
            String cachedCode = getCache(CacheType.VERIFICATION_CODE).get("user456", String.class);
            assertThat(cachedCode).isNotNull();
            assertThat(cachedCode).hasSize(6);
        }
    }

    @DisplayName("인증 코드 확인")
    @Nested
    class AdditionalCheckVerificationCode {

        @DisplayName("인증 성공 시 캐시가 삭제된다")
        @Test
        void test1() {
            // given
            SellerVerificationContext context = mock(SellerVerificationContext.class);
            given(context.getUserId()).willReturn("user123");
            given(context.getVerificationCode()).willReturn("123456");
            
            getCache(CacheType.VERIFICATION_CODE).put("user123", "123456");
            getCache(CacheType.VERIFICATION_TRY).put("user123", new AtomicInteger(2));

            // when
            smsApplication.checkVerificationCode(context);

            // then
            assertThat(getCache(CacheType.VERIFICATION_CODE).get("user123")).isNull();
            assertThat(getCache(CacheType.VERIFICATION_TRY).get("user123")).isNull();
        }

        @DisplayName("빈 문자열 인증 코드는 실패한다")
        @Test
        void test2() {
            // given
            SellerVerificationContext context = mock(SellerVerificationContext.class);
            given(context.getUserId()).willReturn("user123");
            given(context.getVerificationCode()).willReturn("123456");
            
            getCache(CacheType.VERIFICATION_CODE).put("user123", "");

            // when then
            assertThatThrownBy(() -> smsApplication.checkVerificationCode(context))
                .isInstanceOf(CustomException.class);
        }

        @DisplayName("10회 실패 시 사용자가 차단된다")
        @Test
        void test3() {
            // given
            SellerVerificationContext context = mock(SellerVerificationContext.class);
            given(context.getUserId()).willReturn("user123");
            given(context.getVerificationCode()).willReturn("code");
            
            getCache(CacheType.VERIFICATION_CODE).put("user123", "123456");
            getCache(CacheType.VERIFICATION_TRY).put("user123", new AtomicInteger(9));

            // when then
            assertThatThrownBy(() -> smsApplication.checkVerificationCode(context))
                .isInstanceOf(CustomException.class);

            
            assertThat(getCache(CacheType.VERIFICATION_TRY).get("user123")).isNull();
            assertThat(getCache(CacheType.VERIFICATION_BAN_ONE_DAY).get("ban_user", String.class))
                .isEqualTo("user123");
        }

        @DisplayName("연속된 실패 시 카운트가 누적된다")
        @Test
        void test4() {
            // given
            SellerVerificationContext context = mock(SellerVerificationContext.class);
            given(context.getUserId()).willReturn("user123");
            given(context.getVerificationCode()).willReturn("code");
            
            getCache(CacheType.VERIFICATION_CODE).put("user123", "123456");

            // when
            for (int i = 0; i < 5; i++) {
                try {
                    smsApplication.checkVerificationCode(context);
                } catch (CustomException e) {

                }
            }

            // then
            AtomicInteger count = getCache(CacheType.VERIFICATION_TRY).get("user123", AtomicInteger.class);
            assertThat(count).isNotNull();
            assertThat(count.get()).isEqualTo(5);
        }
    }
}
