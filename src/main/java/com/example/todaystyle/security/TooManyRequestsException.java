package com.example.todaystyle.security;

/** 짧은 시간에 같은 출처에서 요청이 반복돼 rate limit에 걸린 경우 (로그인/회원가입 브루트포스 방지). */
public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException() {
        super("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
    }
}
