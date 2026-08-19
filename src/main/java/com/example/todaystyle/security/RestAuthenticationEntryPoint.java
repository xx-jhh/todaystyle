package com.example.todaystyle.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * httpBasic/formLogin을 모두 비활성화하면 Spring Security가 진입점을 못 찾아
 * Http403ForbiddenEntryPoint로 폴백돼 미인증 요청도 403으로 나가던 문제를 해결한다.
 * 이 진입점을 명시적으로 등록해 미인증(401)과 권한없음(403)을 구분하고,
 * 응답 포맷은 {@link com.example.todaystyle.common.GlobalExceptionHandler}와 동일하게 맞춘다.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    public RestAuthenticationEntryPoint(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) {
        resolver.resolveException(request, response, null, new UnauthenticatedException());
    }
}
