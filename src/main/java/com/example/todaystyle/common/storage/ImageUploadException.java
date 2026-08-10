package com.example.todaystyle.common.storage;

/** 이미지 저장소 업로드가 실패한 경우 (네트워크/자격증명 오류 등). */
public class ImageUploadException extends RuntimeException {

    public ImageUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
