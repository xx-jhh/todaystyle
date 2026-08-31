package com.example.todaystyle.user.dto;

import com.example.todaystyle.user.BodyType;
import com.example.todaystyle.user.StyleCategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * 회원가입 요청. 신체 정보/체형/선호 스타일은 선택 입력이며, 현재 프론트는 체형을
 * 질문식 자가진단 퀴즈로 채워 보낸다(신체 치수 입력 UI는 없음). height/weight/waistInch는
 * 향후 프로필 수정 화면 등에서 채워질 수 있어, 값이 오더라도 비현실적인 범위는 거부한다.
 */
public record SignUpRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank @Size(max = 30) String nickname,
        @Min(100) @Max(250) Integer height,
        @Min(20) @Max(300) Integer weight,
        @Min(15) @Max(60) Integer waistInch,
        BodyType bodyType,
        Set<StyleCategory> preferredStyles
) {
}
