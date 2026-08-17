package com.example.todaystyle.recognition;

import com.example.todaystyle.clothing.ClothingCategory;
import com.example.todaystyle.clothing.Fit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Gemini(멀티모달 LLM)에게 OOTD 사진을 던지고, ClothingItem 스키마(category/color/fit)에
 * 맞는 구조화된 JSON을 곧바로 받는다. 전용 Fashion Vision API와 달리 커스텀 fit 분류
 * (SLIM/REGULAR/LOOSE/OVERSIZED)까지 프롬프트만으로 바로 매핑할 수 있어 선택했다.
 */
@Component
public class GeminiClothingRecognitionService implements ClothingRecognitionService {

    private static final Logger log = LoggerFactory.getLogger(GeminiClothingRecognitionService.class);

    private static final String PROMPT = """
            이 사진은 한 사람의 오늘의 착장(OOTD) 사진이다. 사진에서 식별 가능한 개별 옷 아이템을
            모두 찾아서 각각의 category, 대표 color(6자리 hex, 예: #3366CC), fit을 추출하라.
            액세서리나 신발처럼 작은 아이템은 명확히 보일 때만 포함하고, 불확실하면 생략하라.
            """;

    private final RestClient geminiRestClient;
    private final RecognitionProperties properties;
    private final ObjectMapper objectMapper;

    public GeminiClothingRecognitionService(
            RestClient geminiRestClient,
            RecognitionProperties properties,
            ObjectMapper objectMapper
    ) {
        this.geminiRestClient = geminiRestClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<DetectedClothingItem> recognize(byte[] imageBytes, String contentType) {
        if (!properties.isConfigured()) {
            throw new RecognitionUnavailableException(
                    "Gemini API 키가 설정되지 않았습니다. GEMINI_API_KEY 를 설정하세요.");
        }

        String mimeType = contentType != null ? contentType : "image/jpeg";
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(
                                Map.of("text", PROMPT),
                                Map.of("inline_data", Map.of(
                                        "mime_type", mimeType,
                                        "data", Base64.getEncoder().encodeToString(imageBytes)))
                        )
                )),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "responseSchema", responseSchema()
                )
        );

        JsonNode root;
        try {
            root = geminiRestClient.post()
                    .uri(uri -> uri.path("/v1beta/models/{model}:generateContent")
                            .queryParam("key", properties.apiKey())
                            .build(properties.model()))
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RuntimeException e) {
            throw new RecognitionUnavailableException("Gemini API 호출에 실패했습니다.", e);
        }
        if (root == null) {
            throw new RecognitionUnavailableException("Gemini API 응답이 비어 있습니다.");
        }

        String text = root.path("candidates").path(0).path("content").path("parts").path(0)
                .path("text").asString(null);
        if (text == null) {
            throw new RecognitionUnavailableException("Gemini 응답에서 인식 결과 텍스트를 찾을 수 없습니다: " + root);
        }

        JsonNode items;
        try {
            items = objectMapper.readTree(text);
        } catch (RuntimeException e) {
            throw new RecognitionUnavailableException("Gemini 응답 JSON 파싱에 실패했습니다: " + text, e);
        }

        List<DetectedClothingItem> result = new ArrayList<>();
        for (JsonNode item : items) {
            DetectedClothingItem detected = toDetectedItem(item);
            if (detected != null) {
                result.add(detected);
            }
        }
        return result;
    }

    /** 개별 아이템의 category/fit이 스키마 enum과 어긋나면(모델의 드문 오답) 해당 아이템만 건너뛴다. */
    private DetectedClothingItem toDetectedItem(JsonNode item) {
        String categoryValue = item.path("category").asString(null);
        String colorValue = item.path("color").asString(null);
        String fitValue = item.path("fit").asString(null);
        try {
            ClothingCategory category = ClothingCategory.valueOf(categoryValue);
            Fit fit = fitValue == null ? null : Fit.valueOf(fitValue);
            return new DetectedClothingItem(category, colorValue, fit);
        } catch (IllegalArgumentException | NullPointerException e) {
            log.warn("Gemini 인식 결과 중 매핑할 수 없는 아이템을 건너뜁니다: {}", item);
            return null;
        }
    }

    private Map<String, Object> responseSchema() {
        return Map.of(
                "type", "ARRAY",
                "items", Map.of(
                        "type", "OBJECT",
                        "properties", Map.of(
                                "category", Map.of(
                                        "type", "STRING",
                                        "enum", enumNames(ClothingCategory.values())),
                                "color", Map.of(
                                        "type", "STRING",
                                        "description", "대표 색상의 6자리 hex 코드 (예: #3366CC)"),
                                "fit", Map.of(
                                        "type", "STRING",
                                        "enum", enumNames(Fit.values()))
                        ),
                        "required", List.of("category", "color", "fit")
                )
        );
    }

    private List<String> enumNames(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).toList();
    }
}
