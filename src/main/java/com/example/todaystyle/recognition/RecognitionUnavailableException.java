package com.example.todaystyle.recognition;

/** 이미지 인식 API 호출이 불가능하거나 실패한 경우 (API 키 미설정, 외부 오류, 응답 파싱 실패 등). */
public class RecognitionUnavailableException extends RuntimeException {

    public RecognitionUnavailableException(String message) {
        super(message);
    }

    public RecognitionUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
