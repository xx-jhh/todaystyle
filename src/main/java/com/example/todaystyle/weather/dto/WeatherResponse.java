package com.example.todaystyle.weather.dto;

/**
 * 오늘의 날씨 요약 + 코디 팁 응답.
 */
public record WeatherResponse(
        double currentTemp,
        Double minTemp,
        Double maxTemp,
        String sky,
        String precipitation,
        Integer precipProbability,
        String outfitTip
) {
}
