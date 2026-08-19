package com.example.todaystyle.security;

/** 인증이 필요한 요청에 유효한 토큰이 없거나 만료된 경우. */
public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException() {
        super("인증이 필요합니다.");
    }
}
