package com.dreamlog.auth.dto;

public record TokenResponse(
        String accessToken,
        long expiresIn
) {}
