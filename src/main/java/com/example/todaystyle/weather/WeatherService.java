package com.example.todaystyle.weather;

import com.example.todaystyle.weather.KmaWeatherClient.CurrentWeather;
import com.example.todaystyle.weather.KmaWeatherClient.TodayForecast;
import com.example.todaystyle.weather.dto.WeatherResponse;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    private final KmaWeatherClient client;

    public WeatherService(KmaWeatherClient client) {
        this.client = client;
    }

    public WeatherResponse getWeather(double lat, double lon) {
        GridConverter.Grid grid = GridConverter.toGrid(lat, lon);
        CurrentWeather current = client.getCurrent(grid.nx(), grid.ny());
        TodayForecast forecast = client.getTodayForecast(grid.nx(), grid.ny());

        String tip = OutfitTip.forWeather(current.temperature(), current.precipitationTypeCode());

        return new WeatherResponse(
                current.temperature(),
                forecast.minTemp(),
                forecast.maxTemp(),
                WeatherCodes.sky(forecast.skyCode()),
                WeatherCodes.precipitation(current.precipitationTypeCode()),
                forecast.precipProbability(),
                tip);
    }
}
