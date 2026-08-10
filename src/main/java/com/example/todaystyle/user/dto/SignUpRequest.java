package com.example.todaystyle.user.dto;

import com.example.todaystyle.user.BodyType;
import com.example.todaystyle.user.StyleCategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청. 신체 정보/체형/선호 스타일은 선택 입력이며, 이미지 기반 체형 선택 UI가
 * 정해지면 프론트에서 채워 보낸다.
 */
public record SignUpRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank @Size(max = 30) String nickname,
        @Positive Integer height,
        @Positive Integer weight,
        @Positive Integer waistInch,
        BodyType bodyType,
        StyleCategory preferredStyle
) {
}
