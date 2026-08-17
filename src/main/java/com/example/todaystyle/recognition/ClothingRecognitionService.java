package com.example.todaystyle.recognition;

import java.util.List;

/**
 * OOTD 사진에서 개별 옷 아이템(category/color/fit)을 추출한다.
 * 공급자 교체가 쉽도록 인터페이스로 추상화한다({@link com.example.todaystyle.common.storage.ImageStorageService}와 동일한 방식).
 */
public interface ClothingRecognitionService {

    List<DetectedClothingItem> recognize(byte[] imageBytes, String contentType);
}
