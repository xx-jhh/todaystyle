package com.example.todaystyle.recognition;

import com.example.todaystyle.clothing.ClothingCategory;
import com.example.todaystyle.clothing.Fit;

/** 이미지 인식 파이프라인이 사진 한 장에서 추출한 옷 아이템 하나. */
public record DetectedClothingItem(ClothingCategory category, String color, Fit fit) {
}
