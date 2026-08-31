package com.example.todaystyle.ootd;

/** 업로드된 파일이 비어 있거나 이미지가 아닌 경우 (Content-Type 헤더 또는 실제 파일 시그니처 검증 실패). */
public class InvalidImageException extends RuntimeException {

    public InvalidImageException(String message) {
        super(message);
    }
}
