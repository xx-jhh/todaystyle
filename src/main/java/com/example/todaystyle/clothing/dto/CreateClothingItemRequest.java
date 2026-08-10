package com.example.todaystyle.clothing.dto;

import com.example.todaystyle.clothing.ClothingCategory;
import com.example.todaystyle.clothing.Fit;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 옷 아이템 수동 등록 요청. 이미지 인식 API가 연동되기 전까지 사용자가 직접 태깅하는 용도이며,
 * 추후 인식 파이프라인이 이 값들을 자동으로 채운다.
 * color 는 대표 색상 hex (예: "#3366CC"). 인식 API도 보통 hex/RGB를 반환한다.
 */
public record CreateClothingItemRequest(
        @NotNull ClothingCategory category,
        @Pattern(regexp = "^#?[0-9a-fA-F]{6}$", message = "color는 6자리 hex여야 합니다 (예: #3366CC)")
        String color,
        Fit fit,
        String imageUrl
) {
}
