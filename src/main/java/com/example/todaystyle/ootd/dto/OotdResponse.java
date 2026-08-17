package com.example.todaystyle.ootd.dto;

import com.example.todaystyle.ootd.OotdRecord;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record OotdResponse(
        Long id,
        LocalDate recordDate,
        String photoUrl,
        LocalDateTime createdAt,
        Weather weather,
        String memo
) {
    /** 업로드 시점 날씨 스냅샷. 조회 실패/미제공 시 record 자체가 null. */
    public record Weather(Double temp, String sky, String precipitation) {
    }

    public static OotdResponse from(OotdRecord record) {
        Weather weather = record.getWeatherTemp() == null
                ? null
                : new Weather(record.getWeatherTemp(), record.getWeatherSky(), record.getWeatherPrecipitation());

        return new OotdResponse(
                record.getId(),
                record.getRecordDate(),
                record.getPhotoUrl(),
                record.getCreatedAt(),
                weather,
                record.getMemo()
        );
    }
}
