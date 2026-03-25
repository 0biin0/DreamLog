package com.dreamlog.auth;

import com.dreamlog.auth.dto.LoginRequest;
import com.dreamlog.auth.dto.SignupRequest;
import com.dreamlog.auth.dto.TokenResponse;
import com.dreamlog.global.exception.BusinessException;
import com.dreamlog.global.exception.ErrorCode;
import com.dreamlog.global.security.JwtTokenProvider;
import com.dreamlog.user.User;
import com.dreamlog.user.UserRepository;
import com.dreamlog.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResult signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();
        userRepository.save(user);

        return createAuthResult(user);
    }

    @Transactional
    public AuthResult login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        return createAuthResult(user);
    }

    @Transactional
    public TokenResponse refresh(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }

        // Rotate: delete old, create new
        refreshTokenRepository.delete(refreshToken);

        String newRt = jwtTokenProvider.generateRefreshToken();
        RefreshToken newRefreshToken = RefreshToken.builder()
                .userId(refreshToken.getUserId())
                .token(newRt)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiry() / 1000))
                .build();
        refreshTokenRepository.save(newRefreshToken);

        String accessToken = jwtTokenProvider.generateAccessToken(refreshToken.getUserId());
        return new TokenResponse(accessToken, jwtTokenProvider.getAccessTokenExpiry());
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        refreshTokenRepository.findByToken(refreshTokenStr)
                .ifPresent(refreshTokenRepository::delete);
    }

    public String getRefreshTokenValue(AuthResult result) {
        return result.refreshToken();
    }

    private AuthResult createAuthResult(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        RefreshToken rt = RefreshToken.builder()
                .userId(user.getId())
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiry() / 1000))
                .build();
        refreshTokenRepository.save(rt);

        return new AuthResult(
                new TokenResponse(accessToken, jwtTokenProvider.getAccessTokenExpiry()),
                refreshToken,
                UserResponse.from(user)
        );
    }

    public record AuthResult(TokenResponse tokenResponse, String refreshToken, UserResponse user) {}
}
