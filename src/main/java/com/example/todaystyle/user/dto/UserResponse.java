package com.example.todaystyle.user.dto;

import com.example.todaystyle.user.BodyType;
import com.example.todaystyle.user.StyleCategory;
import com.example.todaystyle.user.User;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        Integer height,
        Integer weight,
        Integer waistInch,
        BodyType bodyType,
        StyleCategory preferredStyle
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getHeight(),
                user.getWeight(),
                user.getWaistInch(),
                user.getBodyType(),
                user.getPreferredStyle()
        );
    }
}
