package com.example.todaystyle.clothing;

import com.example.todaystyle.clothing.dto.ClothingItemResponse;
import com.example.todaystyle.clothing.dto.CreateClothingItemRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClothingItemController {

    private final ClothingItemService clothingItemService;

    public ClothingItemController(ClothingItemService clothingItemService) {
        this.clothingItemService = clothingItemService;
    }

    /** OOTD 기록에 옷 아이템 수동 등록 (이미지 인식 연동 전 태깅용). */
    @PostMapping("/api/ootd/{ootdId}/items")
    public ResponseEntity<ClothingItemResponse> addItem(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long ootdId,
            @Valid @RequestBody CreateClothingItemRequest request
    ) {
        ClothingItemResponse response = clothingItemService.addToOotd(userId, ootdId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** 내 옷장 (등록된 모든 아이템). */
    @GetMapping("/api/clothing-items")
    public List<ClothingItemResponse> listMine(@AuthenticationPrincipal Long userId) {
        return clothingItemService.listMine(userId);
    }
}
