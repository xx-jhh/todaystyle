package com.example.todaystyle.ootd;

/** 업로드된 파일이 비어 있거나 이미지가 아닌 경우 (Content-Type 검증 실패). */
public class InvalidImageException extends RuntimeException {

    public InvalidImageException(String message) {
        super(message);
    }
}
