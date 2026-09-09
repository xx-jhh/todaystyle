package com.example.todaystyle.clothing;

import com.example.todaystyle.clothing.dto.ClothingItemResponse;
import com.example.todaystyle.clothing.dto.CreateClothingItemRequest;
import com.example.todaystyle.clothing.dto.UpdateClothingItemRequest;
import com.example.todaystyle.common.PageResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    /** 특정 OOTD 기록에 태깅된 옷 아이템 목록 (수동 태깅 화면에서 이미 등록된 것을 먼저 보여줄 때 사용). */
    @GetMapping("/api/ootd/{ootdId}/items")
    public List<ClothingItemResponse> listByOotd(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long ootdId
    ) {
        return clothingItemService.listByOotd(userId, ootdId);
    }

    /** 내 옷장 (등록된 모든 아이템, 페이지네이션). */
    @GetMapping("/api/clothing-items")
    public PageResponse<ClothingItemResponse> listMine(
            @AuthenticationPrincipal Long userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return clothingItemService.listMine(userId, pageable);
    }

    /** 옷 아이템 단건 조회 (수정 화면 진입용). */
    @GetMapping("/api/clothing-items/{id}")
    public ClothingItemResponse get(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return clothingItemService.get(userId, id);
    }

    /** 자동 인식(Gemini) 결과가 틀렸을 때 카테고리/색상/핏을 직접 수정한다. */
    @PatchMapping("/api/clothing-items/{id}")
    public ClothingItemResponse update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateClothingItemRequest request
    ) {
        return clothingItemService.update(userId, id, request);
    }

    /** 잘못 인식됐거나 필요 없는 옷 아이템을 삭제한다. */
    @DeleteMapping("/api/clothing-items/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        clothingItemService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
