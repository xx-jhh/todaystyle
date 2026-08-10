package com.example.todaystyle.common.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yaml의 cloudinary.* 설정 바인딩. 값이 비어 있어도 앱은 부팅되며,
 * 실제 업로드를 호출하는 시점에만 유효한 값이 필요하다.
 */
@ConfigurationProperties(prefix = "cloudinary")
public record CloudinaryProperties(String cloudName, String apiKey, String apiSecret) {

    public boolean isConfigured() {
        return hasText(cloudName) && hasText(apiKey) && hasText(apiSecret);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
