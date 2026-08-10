package com.example.todaystyle.clothing;

import com.example.todaystyle.clothing.dto.ClothingItemResponse;
import com.example.todaystyle.clothing.dto.CreateClothingItemRequest;
import com.example.todaystyle.ootd.OotdNotFoundException;
import com.example.todaystyle.ootd.OotdRecord;
import com.example.todaystyle.ootd.OotdRepository;
import com.example.todaystyle.user.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClothingItemService {

    private final ClothingItemRepository clothingItemRepository;
    private final OotdRepository ootdRepository;
    private final UserRepository userRepository;

    public ClothingItemService(
            ClothingItemRepository clothingItemRepository,
            OotdRepository ootdRepository,
            UserRepository userRepository
    ) {
        this.clothingItemRepository = clothingItemRepository;
        this.ootdRepository = ootdRepository;
        this.userRepository = userRepository;
    }

    /** 특정 OOTD 기록에 옷 아이템을 수동 등록한다 (이미지 인식 연동 전까지의 태깅 경로). */
    @Transactional
    public ClothingItemResponse addToOotd(Long userId, Long ootdRecordId, CreateClothingItemRequest request) {
        OotdRecord record = ootdRepository.findByIdAndUserId(ootdRecordId, userId)
                .orElseThrow(() -> new OotdNotFoundException(ootdRecordId));

        ClothingItem item = new ClothingItem();
        item.setUser(userRepository.getReferenceById(userId));
        item.setOotdRecord(record);
        item.setCategory(request.category());
        item.setColor(normalizeHex(request.color()));
        item.setFit(request.fit());
        item.setImageUrl(request.imageUrl());

        return ClothingItemResponse.from(clothingItemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<ClothingItemResponse> listMine(Long userId) {
        return clothingItemRepository.findByUserIdWithOotd(userId).stream()
                .map(ClothingItemResponse::from)
                .toList();
    }

    /** 색상 값을 "#RRGGBB" 형태로 정규화한다. null은 그대로 둔다. */
    private String normalizeHex(String color) {
        if (color == null || color.isBlank()) {
            return null;
        }
        String trimmed = color.trim();
        return trimmed.startsWith("#") ? trimmed : "#" + trimmed;
    }
}
