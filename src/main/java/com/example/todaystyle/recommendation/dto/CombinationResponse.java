package com.example.todaystyle.recommendation.dto;

import com.example.todaystyle.clothing.dto.ClothingItemResponse;

/**
 * 추천된 코디 조합 한 건. 서로 다른 날짜에 촬영됐고 실제로 함께 입은 적 없는
 * 상의×하의 페어와, 어울림 점수(0~1) 및 근거 설명을 담는다.
 */
public record CombinationResponse(
        ClothingItemResponse top,
        ClothingItemResponse bottom,
        double score,
        String reason
) {
}
