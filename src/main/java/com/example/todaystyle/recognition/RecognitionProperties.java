package com.example.todaystyle.recognition;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yaml의 gemini.* 설정 바인딩. apiKey가 비어 있어도 앱은 부팅되며,
 * 실제 OOTD 업로드 시 아이템 인식을 호출하는 시점에만 유효한 값이 필요하다.
 */
@ConfigurationProperties(prefix = "gemini")
public record RecognitionProperties(String apiKey, String model) {

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
