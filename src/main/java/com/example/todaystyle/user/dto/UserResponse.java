package com.example.todaystyle.user.dto;

import com.example.todaystyle.user.BodyType;
import com.example.todaystyle.user.StyleCategory;
import com.example.todaystyle.user.User;
import java.util.Set;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        Integer height,
        Integer weight,
        Integer waistInch,
        BodyType bodyType,
        Set<StyleCategory> preferredStyles
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
                user.getPreferredStyles()
        );
    }
}
