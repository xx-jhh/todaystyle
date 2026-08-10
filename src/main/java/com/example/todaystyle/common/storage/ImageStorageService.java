package com.example.todaystyle.common.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 이미지 저장소 추상화. 현재 구현은 Cloudinary이지만, 저장소를 바꾸더라도
 * 이 인터페이스에 의존하는 서비스 코드는 그대로 둘 수 있도록 분리한다.
 */
public interface ImageStorageService {

    /**
     * 이미지를 업로드하고 접근 가능한 URL을 반환한다.
     *
     * @param file   업로드할 이미지 파일
     * @param folder 저장소 내 폴더 경로 (예: "todaystyle/ootd/42")
     * @return 저장된 이미지의 공개 URL
     */
    String upload(MultipartFile file, String folder);
}
