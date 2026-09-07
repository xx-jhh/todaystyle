package com.example.todaystyle.common;

import com.example.todaystyle.security.RateLimitInterceptor;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    public WebMvcConfig(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/auth/signup", "/api/auth/login",
                        "/api/auth/password-reset/request", "/api/auth/password-reset/confirm");
    }

    /**
     * JSON 응답 스펙(RFC 8259)상 UTF-8이 기본이라 Spring이 Content-Type에 charset을 생략하는데,
     * 이걸 그대로 받는 일부 클라이언트(IntelliJ HTTP Client 등)가 한글을 다른 인코딩으로
     * 오추측해 깨져 보이는 문제가 있어 명시적으로 UTF-8을 붙인다.
     */
    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        builder.configureMessageConverters(converter -> {
            if (converter instanceof JacksonJsonHttpMessageConverter jsonConverter) {
                jsonConverter.setDefaultCharset(StandardCharsets.UTF_8);
            }
        });
    }

    /** 프론트(React Router SPA)를 같은 오리진에서 서빙하기 위한 정적 리소스 + 폴백 설정. */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Vite가 내용이 바뀌면 파일명 해시도 같이 바꿔주므로 /assets/** 는 영구 캐시해도 안전하다.
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable());

        // index.html, manifest.webmanifest, sw.js 등은 배포할 때마다 내용이 바뀌는데 파일명은
        // 그대로다. 이걸 브라우저가 캐시해버리면 새로 배포한 뒤에도 옛 index.html이 계속
        // 서빙되면서 이미 지워진 옛 해시의 JS/CSS(/assets/index-OLDHASH.js)를 계속 요청하게 되고,
        // 그 요청은 404가 나서 리액트가 마운트를 못 하고 흰 화면만 남는다. 그래서 이 경로들은
        // 항상 서버에 재검증하도록(no-cache) 강제한다.
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.noCache())
                .resourceChain(true)
                .addResolver(new SpaFallbackResourceResolver());
    }
}
