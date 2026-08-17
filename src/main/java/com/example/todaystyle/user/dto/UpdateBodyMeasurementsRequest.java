package com.example.todaystyle.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** 마이페이지에서 신체 치수를 수정하는 요청. 필드를 비우면(null) 해당 값을 지운다. */
public record UpdateBodyMeasurementsRequest(
        @Min(100) @Max(250) Integer height,
        @Min(20) @Max(300) Integer weight,
        @Min(15) @Max(60) Integer waistInch
) {
}
