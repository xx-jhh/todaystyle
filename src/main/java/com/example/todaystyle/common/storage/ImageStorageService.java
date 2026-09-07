package com.example.todaystyle.common.storage;

/**
 * 이미지 저장소 추상화. 현재 구현은 Cloudinary이지만, 저장소를 바꾸더라도
 * 이 인터페이스에 의존하는 서비스 코드는 그대로 둘 수 있도록 분리한다.
 *
 * <p>MultipartFile이 아니라 byte[]를 받는 이유: 호출하는 쪽(OotdService)이 비동기 보강
 * 이벤트에도 같은 바이트가 필요해서 어차피 한 번 읽어야 하는데, 여기서 MultipartFile을
 * 또 읽으면 같은 파일을 두 번 읽게 된다. 호출자가 한 번 읽은 바이트를 그대로 넘긴다.
 */
public interface ImageStorageService {

    /**
     * 이미지를 업로드하고 접근 가능한 URL(+삭제에 필요한 publicId)을 반환한다.
     *
     * @param imageBytes 업로드할 이미지 바이트
     * @param folder     저장소 내 폴더 경로 (예: "todaystyle/ootd/42")
     * @return 저장된 이미지의 공개 URL과 publicId
     */
    UploadedImage upload(byte[] imageBytes, String folder);

    /**
     * 업로드했지만 이후 단계(DB 저장 등)가 실패해서 더 이상 참조되지 않는 이미지를 정리한다.
     * 이미 다른 에러를 처리하는 흐름 중에 호출되는 보상 동작이라, 삭제 자체가 실패해도
     * 예외를 던지지 않고 내부에서 로그만 남긴다 — 원래 에러 응답을 가리면 안 되기 때문이다.
     *
     * @param publicId {@link #upload}가 반환한 {@link UploadedImage#publicId()}
     */
    void delete(String publicId);
}
