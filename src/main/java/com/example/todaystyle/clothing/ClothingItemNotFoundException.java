package com.example.todaystyle.clothing;

/** 내 소유가 아니거나 존재하지 않는 옷 아이템에 접근한 경우. */
public class ClothingItemNotFoundException extends RuntimeException {

    public ClothingItemNotFoundException(Long id) {
        super("옷 아이템을 찾을 수 없습니다: " + id);
    }
}
