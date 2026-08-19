package com.example.todaystyle.ootd;

/**
 * OOTD 업로드(사진 저장 + DB 저장)가 끝난 뒤 발행되는 이벤트. 응답 속도에 영향을 주면 안 되는
 * 부가 작업(날씨 스냅샷 조회, 옷 아이템 자동 인식)을 {@link OotdEnrichmentListener}가
 * 비동기로 이어받아 처리하는 데 쓰인다.
 */
public record OotdUploadedEvent(
        Long ootdRecordId,
        Long userId,
        Double lat,
        Double lon,
        byte[] imageBytes,
        String imageContentType
) {
}
