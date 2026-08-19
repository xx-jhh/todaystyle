package com.example.todaystyle.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** 로그인/회원가입 브루트포스 방지: 클라이언트 IP+요청 경로 단위로 분당 요청 수를 제한한다. */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter rateLimiter;

    public RateLimitInterceptor(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        String key = request.getRemoteAddr() + ":" + request.getRequestURI();
        if (!rateLimiter.tryAcquire(key)) {
            throw new TooManyRequestsException();
        }
        return true;
    }
}
