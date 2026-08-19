package com.example.todaystyle.ootd;

import com.example.todaystyle.clothing.ClothingItemRepository;
import com.example.todaystyle.common.storage.ImageStorageService;
import com.example.todaystyle.ootd.dto.OotdResponse;
import com.example.todaystyle.user.UserRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OotdService {

    private final OotdRepository ootdRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;
    private final ApplicationEventPublisher eventPublisher;
    private final ClothingItemRepository clothingItemRepository;

    public OotdService(
            OotdRepository ootdRepository,
            UserRepository userRepository,
            ImageStorageService imageStorageService,
            ApplicationEventPublisher eventPublisher,
            ClothingItemRepository clothingItemRepository
    ) {
        this.ootdRepository = ootdRepository;
        this.userRepository = userRepository;
        this.imageStorageService = imageStorageService;
        this.eventPublisher = eventPublisher;
        this.clothingItemRepository = clothingItemRepository;
    }

    /**
     * 날씨 스냅샷 조회·옷 아이템 자동 인식(Gemini)은 실패해도 업로드 자체엔 영향 없는
     * 부가 작업인데도 예전에는 응답 전에 순차로 기다려 업로드가 느렸다. 이제 사진 저장과
     * OOTD 레코드 저장까지만 여기서 동기 처리하고, 나머지는 {@link OotdUploadedEvent}로
     * 발행해 {@link OotdEnrichmentListener}가 커밋 후 비동기로 채우도록 넘긴다.
     */
    @Transactional
    public OotdResponse upload(Long userId, LocalDate recordDate, MultipartFile image, Double lat, Double lon) {
        validateImage(image);

        if (ootdRepository.existsByUserIdAndRecordDate(userId, recordDate)) {
            throw new OotdAlreadyExistsException(recordDate);
        }

        // 바이트를 먼저 한 번만 읽어서 Cloudinary 업로드와 비동기 보강 이벤트가 같은 배열을
        // 공유한다 — MultipartFile을 그대로 두 번 읽는 낭비를 없애고, 바이트 읽기가 Cloudinary
        // 호출보다 먼저 일어나므로 "업로드는 성공했는데 그다음 단계가 실패해서 저장소에 참조
        // 없는 사진이 남는" 상황 자체가 생기지 않는다.
        byte[] imageBytes = readBytes(image);
        String photoUrl = imageStorageService.upload(imageBytes, "todaystyle/ootd/" + userId);

        OotdRecord record = new OotdRecord();
        record.setUser(userRepository.getReferenceById(userId));
        record.setRecordDate(recordDate);
        record.setPhotoUrl(photoUrl);
        OotdRecord saved = ootdRepository.save(record);

        eventPublisher.publishEvent(
                new OotdUploadedEvent(saved.getId(), userId, lat, lon, imageBytes, image.getContentType()));

        return OotdResponse.from(saved);
    }

    private byte[] readBytes(MultipartFile image) {
        try {
            return image.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Cloudinary/인식 파이프라인까지 가기 전에, 비어 있거나 이미지가 아닌 파일을 걸러낸다. */
    private void validateImage(MultipartFile image) {
        if (image.isEmpty()) {
            throw new InvalidImageException("이미지 파일이 비어 있습니다.");
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidImageException("이미지 파일만 업로드할 수 있습니다.");
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

    /**
     * 잘못 찍었거나 마음에 안 드는 OOTD를 지우고 다시 올릴 수 있게 한다. 이 기록에서
     * 추출된 옷 아이템(수동 등록/자동 인식 모두)도 함께 지운다 — FK 제약상 먼저 지워야
     * OotdRecord를 지울 수 있다. Cloudinary에 남은 원본 사진 파일은 지우지 않는다(무료
     * 티어라 당장은 비용 영향이 미미하고, 별도 정리는 이후 과제로 남겨둠).
     */
    @Transactional
    public void delete(Long userId, Long id) {
        OotdRecord record = ootdRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new OotdNotFoundException(id));
        clothingItemRepository.deleteByOotdRecordId(record.getId());
        ootdRepository.delete(record);
    }
}
