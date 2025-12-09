package com.search.listener;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

import com.search.dto.UserBehaviorDto;

import lombok.extern.slf4j.Slf4j;

/**
 * 배치 처리 중 발생하는 예외에 대한 스킵(Skip) 리스너
 * 잘못된 형식의 로그 라인 등을 건너뛰고 로깅
 */
@Slf4j
@Component
public class SearchBatchSkipListener implements SkipListener<UserBehaviorDto, UserBehaviorDto> {

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("Skip in Read - Exception: {}, Type: {}, Stacktrace: {}",
                t.getMessage(),
                t.getClass().getSimpleName(),
                getStackTraceAsString(t));
    }

    @Override
    public void onSkipInWrite(UserBehaviorDto item, Throwable t) {
        log.warn("Skip in Write - Item: {}, Exception: {}, Type: {}",
                maskSensitiveData(item),
                t.getMessage(),
                t.getClass().getSimpleName());
    }

    @Override
    public void onSkipInProcess(UserBehaviorDto item, Throwable t) {
        log.warn("Skip in Process - Item: {}, Exception: {}, Type: {}",
                maskSensitiveData(item),
                t.getMessage(),
                t.getClass().getSimpleName());
    }

    /**
     * 민감 정보 마스킹 처리
     */
    private String maskSensitiveData(UserBehaviorDto item) {
        if (item == null) {
            return "null";
        }
        return String.format("UserBehavior(userId=%s, type=%s, valueLength=%d)",
                item.userId() != null ? "***" : "null",
                item.behaviorType(),
                item.behaviorValue() != null ? item.behaviorValue().length() : 0);
    }

    /**
     * 스택트레이스를 문자열로 변환
     */
    private String getStackTraceAsString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}
