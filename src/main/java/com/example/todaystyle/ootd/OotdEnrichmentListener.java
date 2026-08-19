package com.example.todaystyle.ootd;

import com.example.todaystyle.clothing.ClothingItemService;
import com.example.todaystyle.recognition.ClothingRecognitionService;
import com.example.todaystyle.recognition.DetectedClothingItem;
import com.example.todaystyle.weather.WeatherService;
import com.example.todaystyle.weather.dto.WeatherResponse;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * OOTD 업로드 응답 속도가 외부 API(기상청/Gemini) 지연에 발목 잡히지 않도록, 실패해도
 * 업로드 자체엔 영향 없는 부가 작업만 골라 업로드 트랜잭션이 커밋된 뒤 비동기로 처리한다.
 * {@code @TransactionalEventListener(AFTER_COMMIT)}로 OotdRecord가 실제로 커밋되어
 * 다른 트랜잭션에서 조회 가능해진 뒤에만 실행되도록 보장하고(그렇지 않으면 아직 커밋 전인
 * 레코드를 별도 스레드에서 findById로 못 찾는 레이스가 생긴다), {@code @Async}로 업로드
 * 요청 스레드와 별개 스레드에서 돌려 응답을 막지 않는다.
 */
@Component
public class OotdEnrichmentListener {

    private static final Logger log = LoggerFactory.getLogger(OotdEnrichmentListener.class);

    private final OotdRepository ootdRepository;
    private final WeatherService weatherService;
    private final ClothingRecognitionService clothingRecognitionService;
    private final ClothingItemService clothingItemService;

    public OotdEnrichmentListener(
            OotdRepository ootdRepository,
            WeatherService weatherService,
            ClothingRecognitionService clothingRecognitionService,
            ClothingItemService clothingItemService
    ) {
        this.ootdRepository = ootdRepository;
        this.weatherService = weatherService;
        this.clothingRecognitionService = clothingRecognitionService;
        this.clothingItemService = clothingItemService;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOotdUploaded(OotdUploadedEvent event) {
        OotdRecord record = ootdRepository.findById(event.ootdRecordId()).orElse(null);
        if (record == null) {
            return;
        }

        if (event.lat() != null && event.lon() != null) {
            fetchWeatherSnapshot(event.lat(), event.lon()).ifPresent(weather -> {
                record.setWeatherTemp(weather.currentTemp());
                record.setWeatherSky(weather.sky());
                record.setWeatherPrecipitation(weather.precipitation());
            });
        }

        List<DetectedClothingItem> detectedItems =
                recognizeClothingItems(event.imageBytes(), event.imageContentType());
        if (!detectedItems.isEmpty()) {
            clothingItemService.saveAutoDetected(event.userId(), record, detectedItems);
        }
    }

    /** 날씨 조회는 부가 정보라 실패해도 업로드 자체는 막지 않는다(best-effort). */
    private Optional<WeatherResponse> fetchWeatherSnapshot(double lat, double lon) {
        try {
            return Optional.of(weatherService.getWeather(lat, lon));
        } catch (RuntimeException e) {
            log.warn("OOTD 업로드 후 날씨 스냅샷 조회 실패 (lat={}, lon={}): {}", lat, lon, e.getMessage());
            return Optional.empty();
        }
    }

    /** 옷 아이템 자동 추출도 부가 기능이라 실패해도 업로드 자체는 막지 않는다(best-effort). */
    private List<DetectedClothingItem> recognizeClothingItems(byte[] imageBytes, String contentType) {
        try {
            return clothingRecognitionService.recognize(imageBytes, contentType);
        } catch (RuntimeException e) {
            log.warn("OOTD 업로드 후 옷 아이템 자동 인식 실패: {}", e.getMessage());
            return List.of();
        }
    }
}
