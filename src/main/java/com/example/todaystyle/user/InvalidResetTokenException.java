package com.example.todaystyle.user;

/** 재설정 토큰이 존재하지 않거나, 이미 사용됐거나, 만료된 경우. */
public class InvalidResetTokenException extends RuntimeException {

    public InvalidResetTokenException() {
        super("유효하지 않거나 만료된 재설정 링크입니다. 다시 요청해주세요.");
    }
}
