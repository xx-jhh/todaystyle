package com.example.todaystyle.ootd;

import com.example.todaystyle.clothing.ClothingItemRepository;
import com.example.todaystyle.common.PageResponse;
import com.example.todaystyle.common.storage.ImageStorageService;
import com.example.todaystyle.common.storage.UploadedImage;
import com.example.todaystyle.ootd.dto.OotdCountResponse;
import com.example.todaystyle.ootd.dto.OotdResponse;
import com.example.todaystyle.user.UserRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        // 여기서 미리 걸러두면 대부분의 경우 Cloudinary 업로드까지 안 가고 바로 끝난다.
        // 다만 동시에 두 요청(더블클릭, 타임아웃 후 재시도 등)이 들어오면 이 체크를 둘 다
        // 통과할 수 있어서, 최종 방어는 아래 DB 유니크 제약(user_id, record_date)이 한다.
        if (ootdRepository.existsByUserIdAndRecordDate(userId, recordDate)) {
            throw new OotdAlreadyExistsException(recordDate);
        }

        // 바이트를 먼저 한 번만 읽어서 Cloudinary 업로드와 비동기 보강 이벤트가 같은 배열을
        // 공유한다 — MultipartFile을 그대로 두 번 읽는 낭비를 없앤다.
        byte[] imageBytes = readBytes(image);
        validateImageSignature(imageBytes);
        UploadedImage uploaded = imageStorageService.upload(imageBytes, "todaystyle/ootd/" + userId);

        OotdRecord record = new OotdRecord();
        record.setUser(userRepository.getReferenceById(userId));
        record.setRecordDate(recordDate);
        record.setPhotoUrl(uploaded.url());

        OotdRecord saved;
        try {
            saved = ootdRepository.save(record);
        } catch (DataIntegrityViolationException e) {
            // 위 existsBy 체크를 두 요청이 동시에 통과해서 여기까지 왔을 때 DB 유니크 제약이
            // 걸리는 경우. 이미 Cloudinary엔 이미지가 올라간 뒤라 그대로 두면 DB 어디에도
            // 참조되지 않는 고아 이미지가 남으므로, 방금 올린 이미지를 지우고 사전 체크와
            // 동일한 409 응답으로 통일한다(500으로 새 나가지 않게).
            imageStorageService.delete(uploaded.publicId());
            throw new OotdAlreadyExistsException(recordDate);
        }

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

    /**
     * Cloudinary/인식 파이프라인까지 가기 전에, 비어 있거나 이미지가 아닌 파일을 걸러낸다.
     * Content-Type 헤더만으로는 클라이언트가 무엇을 보냈다고 "주장"하는지만 알 수 있어서 1차로만
     * 쓰고, 실제 바이트 검증은 {@link #validateImageSignature}가 파일을 다 읽은 뒤 한 번 더 한다.
     */
    private void validateImage(MultipartFile image) {
        if (image.isEmpty()) {
            throw new InvalidImageException("이미지 파일이 비어 있습니다.");
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidImageException("이미지 파일만 업로드할 수 있습니다.");
        }
    }

    /** Content-Type 헤더는 위조 가능하므로, 실제 바이트의 파일 시그니처(매직 바이트)로 다시 확인한다. */
    private void validateImageSignature(byte[] imageBytes) {
        if (!ImageSignature.looksLikeImage(imageBytes)) {
            throw new InvalidImageException("이미지 파일만 업로드할 수 있습니다.");
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<OotdResponse> list(Long userId, Pageable pageable) {
        Page<OotdRecord> page = ootdRepository.findByUserIdOrderByRecordDateDesc(userId, pageable);
        return PageResponse.of(page, OotdResponse::from);
    }

    /** 마이페이지 통계(전체 기록 수)용. 목록 API는 이제 페이지네이션이라 전체 개수를 안 담는다. */
    @Transactional(readOnly = true)
    public OotdCountResponse count(Long userId) {
        return new OotdCountResponse(ootdRepository.countByUserId(userId));
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
