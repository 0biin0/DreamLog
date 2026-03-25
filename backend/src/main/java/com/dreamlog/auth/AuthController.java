package com.dreamlog.auth;

import com.dreamlog.auth.dto.LoginRequest;
import com.dreamlog.auth.dto.SignupRequest;
import com.dreamlog.auth.dto.TokenResponse;
import com.dreamlog.global.common.ApiResponse;
import com.dreamlog.user.dto.UserResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String RT_COOKIE_NAME = "refreshToken";
    private static final int RT_MAX_AGE = 7 * 24 * 60 * 60; // 7 days

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<Map<String, Object>> signup(@Valid @RequestBody SignupRequest request,
                                                    HttpServletResponse response) {
        AuthService.AuthResult result = authService.signup(request);
        setRefreshTokenCookie(response, result.refreshToken());

        return ApiResponse.ok(Map.of(
                "token", result.tokenResponse(),
                "user", result.user()
        ));
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request,
                                                   HttpServletResponse response) {
        AuthService.AuthResult result = authService.login(request);
        setRefreshTokenCookie(response, result.refreshToken());

        return ApiResponse.ok(Map.of(
                "token", result.tokenResponse(),
                "user", result.user()
        ));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);
        TokenResponse tokenResponse = authService.refresh(refreshToken);
        return ApiResponse.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        clearRefreshTokenCookie(response);
        return ApiResponse.ok();
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(RT_COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // dev: false, prod: true
        cookie.setPath("/");
        cookie.setMaxAge(RT_MAX_AGE);
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(RT_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> RT_COOKIE_NAME.equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
