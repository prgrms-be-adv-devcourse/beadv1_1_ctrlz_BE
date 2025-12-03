package com.common.filter;

import java.io.IOException;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * MDC(Mapped Diagnostic Context)를 이용한 로깅 필터
 * 모든 HTTP 요청에 대해 userId와 api 정보를 MDC에 설정하여
 * 로그에서 요청을 추적할 수 있도록 합니다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String USER_ID_MDC_KEY = "userId";
    private static final String API_MDC_KEY = "api";
    private static final String REQUEST_ID_HEADER = "X-REQUEST-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // X-REQUEST-ID 헤더에서 userId 추출 (없으면 ANONYMOUS)
            String userId = request.getHeader(REQUEST_ID_HEADER);
            if (userId == null || userId.isEmpty()) {
                userId = "ANONYMOUS";
            }
            MDC.put(USER_ID_MDC_KEY, userId);

            // HTTP 메서드와 URI를 조합하여 api 정보 설정
            String method = request.getMethod();
            String requestURI = request.getRequestURI();
            MDC.put(API_MDC_KEY, String.format("[%s] %s", method, requestURI));

            filterChain.doFilter(request, response);
        } finally {
            // 요청 처리 후 MDC 정리 (메모리 누수 방지)
            MDC.clear();
        }
    }
}
