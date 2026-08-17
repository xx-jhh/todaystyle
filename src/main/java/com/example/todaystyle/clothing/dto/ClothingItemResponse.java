package com.example.todaystyle.clothing.dto;

import com.example.todaystyle.clothing.ClothingCategory;
import com.example.todaystyle.clothing.ClothingItem;
import com.example.todaystyle.clothing.Fit;
import java.time.LocalDate;

public record ClothingItemResponse(
        Long id,
        Long ootdRecordId,
        LocalDate recordDate,
        ClothingCategory category,
        String color,
        Fit fit,
        String imageUrl,
        String ootdPhotoUrl
) {
    public static ClothingItemResponse from(ClothingItem item) {
        return new ClothingItemResponse(
                item.getId(),
                item.getOotdRecord().getId(),
                item.getOotdRecord().getRecordDate(),
                item.getCategory(),
                item.getColor(),
                item.getFit(),
                item.getImageUrl(),
                item.getOotdRecord().getPhotoUrl()
        );
    }
}
