package com.example.todaystyle.recommendation.dto;

import com.example.todaystyle.clothing.dto.ClothingItemResponse;

/**
 * 추천된 코디 조합 한 건. 서로 다른 날짜에 촬영됐고 실제로 함께 입은 적 없는 페어(상의×하의
 * 또는 원피스×아우터)와, 어울림 점수(0~1) 및 근거 설명을 담는다. 필드명이 "top"/"bottom"이
 * 아니라 "primaryItem"/"secondaryItem"인 이유: 원피스×아우터 페어를 상의/하의로 부르면 실제
 * 카테고리와 안 맞아 오해를 준다 — 실제 카테고리는 각 {@link ClothingItemResponse#category()}로
 * 판별한다.
 */
public record CombinationResponse(
        ClothingItemResponse primaryItem,
        ClothingItemResponse secondaryItem,
        double score,
        String reason
) {
}
