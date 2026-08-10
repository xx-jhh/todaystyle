package com.example.todaystyle.weather;

import com.example.todaystyle.weather.dto.WeatherResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /** 위경도 기준 오늘의 날씨 + 코디 팁. 프론트는 브라우저 위치권한으로 lat/lon을 보낸다. */
    @GetMapping
    public WeatherResponse today(
            @RequestParam double lat,
            @RequestParam double lon
    ) {
        return weatherService.getWeather(lat, lon);
    }
}
