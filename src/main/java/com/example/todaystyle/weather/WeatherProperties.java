package com.example.todaystyle.weather;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yaml의 kma.* 설정 바인딩. serviceKey가 비어 있어도 앱은 부팅되며,
 * 실제 날씨 조회를 호출하는 시점에만 유효한 값이 필요하다.
 */
@ConfigurationProperties(prefix = "kma")
public record WeatherProperties(String baseUrl, String serviceKey) {

    public boolean isConfigured() {
        return serviceKey != null && !serviceKey.isBlank();
    }
}
