package com.example.todaystyle.clothing.dto;

import com.example.todaystyle.clothing.ClothingCategory;
import com.example.todaystyle.clothing.Fit;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 옷 아이템 수정 요청. 이미지 인식(Gemini)이 카테고리/색상/핏을 잘못 판별했을 때
 * 사용자가 직접 바로잡는 용도. imageUrl은 사용자가 입력할 값이 아니라 여기서 다루지 않는다.
 */
public record UpdateClothingItemRequest(
        @NotNull ClothingCategory category,
        @Pattern(regexp = "^#?[0-9a-fA-F]{6}$", message = "color는 6자리 hex여야 합니다 (예: #3366CC)")
        String color,
        Fit fit
) {
}
