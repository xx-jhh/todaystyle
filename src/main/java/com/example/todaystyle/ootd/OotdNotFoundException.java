package com.example.todaystyle.ootd;

/** 존재하지 않거나 다른 사용자의 OOTD에 접근한 경우. */
public class OotdNotFoundException extends RuntimeException {

    public OotdNotFoundException(Long id) {
        super("OOTD를 찾을 수 없습니다: " + id);
    }
}
