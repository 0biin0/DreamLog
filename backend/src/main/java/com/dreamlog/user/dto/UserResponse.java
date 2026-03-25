package com.dreamlog.user.dto;

import com.dreamlog.user.User;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        String profileImage
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getNickname(), user.getProfileImage());
    }
}
