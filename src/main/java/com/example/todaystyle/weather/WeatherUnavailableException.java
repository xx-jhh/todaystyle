package com.example.todaystyle.weather;

/** 기상청 API 조회가 불가능하거나 실패한 경우 (서비스키 미설정, 외부 오류 등). */
public class WeatherUnavailableException extends RuntimeException {

    public WeatherUnavailableException(String message) {
        super(message);
    }

    public WeatherUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
