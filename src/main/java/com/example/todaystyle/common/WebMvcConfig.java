package com.example.todaystyle.common;

import com.example.todaystyle.security.RateLimitInterceptor;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Configuration;
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
                .addPathPatterns("/api/auth/signup", "/api/auth/login");
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
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new SpaFallbackResourceResolver());
    }
}
