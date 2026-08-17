package com.example.todaystyle.ootd;

import com.example.todaystyle.clothing.ClothingItemService;
import com.example.todaystyle.common.storage.ImageStorageService;
import com.example.todaystyle.ootd.dto.OotdResponse;
import com.example.todaystyle.recognition.ClothingRecognitionService;
import com.example.todaystyle.recognition.DetectedClothingItem;
import com.example.todaystyle.user.UserRepository;
import com.example.todaystyle.weather.WeatherService;
import com.example.todaystyle.weather.dto.WeatherResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OotdService {

    private static final Logger log = LoggerFactory.getLogger(OotdService.class);

    private final OotdRepository ootdRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;
    private final WeatherService weatherService;
    private final ClothingRecognitionService clothingRecognitionService;
    private final ClothingItemService clothingItemService;

    public OotdService(
            OotdRepository ootdRepository,
            UserRepository userRepository,
            ImageStorageService imageStorageService,
            WeatherService weatherService,
            ClothingRecognitionService clothingRecognitionService,
            ClothingItemService clothingItemService
    ) {
        this.ootdRepository = ootdRepository;
        this.userRepository = userRepository;
        this.imageStorageService = imageStorageService;
        this.weatherService = weatherService;
        this.clothingRecognitionService = clothingRecognitionService;
        this.clothingItemService = clothingItemService;
    }

    @Transactional
    public OotdResponse upload(Long userId, LocalDate recordDate, MultipartFile image, Double lat, Double lon) {
        if (ootdRepository.existsByUserIdAndRecordDate(userId, recordDate)) {
            throw new OotdAlreadyExistsException(recordDate);
        }

        String photoUrl = imageStorageService.upload(image, "todaystyle/ootd/" + userId);

        OotdRecord record = new OotdRecord();
        record.setUser(userRepository.getReferenceById(userId));
        record.setRecordDate(recordDate);
        record.setPhotoUrl(photoUrl);

        if (lat != null && lon != null) {
            fetchWeatherSnapshot(lat, lon).ifPresent(weather -> {
                record.setWeatherTemp(weather.currentTemp());
                record.setWeatherSky(weather.sky());
                record.setWeatherPrecipitation(weather.precipitation());
            });
        }

        OotdRecord saved = ootdRepository.save(record);

        List<DetectedClothingItem> detectedItems = recognizeClothingItems(image);
        if (!detectedItems.isEmpty()) {
            clothingItemService.saveAutoDetected(userId, saved, detectedItems);
        }

        return OotdResponse.from(saved);
    }

    /** 날씨 조회는 부가 정보라 실패해도 업로드 자체는 막지 않는다(best-effort). */
    private Optional<WeatherResponse> fetchWeatherSnapshot(double lat, double lon) {
        try {
            return Optional.of(weatherService.getWeather(lat, lon));
        } catch (RuntimeException e) {
            log.warn("OOTD 업로드 시 날씨 스냅샷 조회 실패 (lat={}, lon={}): {}", lat, lon, e.getMessage());
            return Optional.empty();
        }
    }

    /** 옷 아이템 자동 추출도 부가 기능이라 실패해도 업로드 자체는 막지 않는다(best-effort). */
    private List<DetectedClothingItem> recognizeClothingItems(MultipartFile image) {
        try {
            return clothingRecognitionService.recognize(image.getBytes(), image.getContentType());
        } catch (IOException | RuntimeException e) {
            log.warn("OOTD 업로드 시 옷 아이템 자동 인식 실패: {}", e.getMessage());
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public List<OotdResponse> list(Long userId) {
        return ootdRepository.findByUserIdOrderByRecordDateDesc(userId).stream()
                .map(OotdResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OotdResponse get(Long userId, Long id) {
        OotdRecord record = ootdRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new OotdNotFoundException(id));
        return OotdResponse.from(record);
    }

    @Transactional
    public OotdResponse updateMemo(Long userId, Long id, String memo) {
        OotdRecord record = ootdRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new OotdNotFoundException(id));
        record.setMemo(memo == null || memo.isBlank() ? null : memo);
        return OotdResponse.from(record);
    }
}
