package com.dreamlog.auth;

import com.dreamlog.auth.dto.LoginRequest;
import com.dreamlog.auth.dto.SignupRequest;
import com.dreamlog.global.exception.BusinessException;
import com.dreamlog.global.exception.ErrorCode;
import com.dreamlog.global.security.JwtTokenProvider;
import com.dreamlog.user.User;
import com.dreamlog.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        SignupRequest request = new SignupRequest("test@example.com", "password123", "테스터");
        given(userRepository.existsByEmail("test@example.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("encoded");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(jwtTokenProvider.generateAccessToken(any())).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken()).willReturn("refresh-token");
        given(jwtTokenProvider.getAccessTokenExpiry()).willReturn(900000L);
        given(jwtTokenProvider.getRefreshTokenExpiry()).willReturn(604800000L);
        given(refreshTokenRepository.save(any(RefreshToken.class))).willAnswer(invocation -> invocation.getArgument(0));

        AuthService.AuthResult result = authService.signup(request);

        assertThat(result.tokenResponse().accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signup_duplicateEmail() {
        SignupRequest request = new SignupRequest("test@example.com", "password123", "테스터");
        given(userRepository.existsByEmail("test@example.com")).willReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = User.builder().email("test@example.com").password("encoded").nickname("테스터").build();
        given(userRepository.findByEmailAndDeletedAtIsNull("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", "encoded")).willReturn(true);
        given(jwtTokenProvider.generateAccessToken(any())).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken()).willReturn("refresh-token");
        given(jwtTokenProvider.getAccessTokenExpiry()).willReturn(900000L);
        given(jwtTokenProvider.getRefreshTokenExpiry()).willReturn(604800000L);
        given(refreshTokenRepository.save(any(RefreshToken.class))).willAnswer(invocation -> invocation.getArgument(0));

        AuthService.AuthResult result = authService.login(request);

        assertThat(result.tokenResponse().accessToken()).isEqualTo("access-token");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_wrongPassword() {
        LoginRequest request = new LoginRequest("test@example.com", "wrong");
        User user = User.builder().email("test@example.com").password("encoded").nickname("테스터").build();
        given(userRepository.findByEmailAndDeletedAtIsNull("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong", "encoded")).willReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_PASSWORD));
    }

    @Test
    @DisplayName("로그인 실패 - 사용자 없음")
    void login_userNotFound() {
        LoginRequest request = new LoginRequest("nonexist@example.com", "password123");
        given(userRepository.findByEmailAndDeletedAtIsNull("nonexist@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("토큰 갱신 성공")
    void refresh_success() {
        RefreshToken rt = RefreshToken.builder()
                .userId(1L)
                .token("old-rt")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        given(refreshTokenRepository.findByToken("old-rt")).willReturn(Optional.of(rt));
        given(jwtTokenProvider.generateAccessToken(1L)).willReturn("new-at");
        given(jwtTokenProvider.generateRefreshToken()).willReturn("new-rt");
        given(jwtTokenProvider.getAccessTokenExpiry()).willReturn(900000L);
        given(jwtTokenProvider.getRefreshTokenExpiry()).willReturn(604800000L);
        given(refreshTokenRepository.save(any(RefreshToken.class))).willAnswer(invocation -> invocation.getArgument(0));

        var result = authService.refresh("old-rt");

        assertThat(result.accessToken()).isEqualTo("new-at");
        verify(refreshTokenRepository).delete(rt);
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 만료됨")
    void refresh_expired() {
        RefreshToken rt = RefreshToken.builder()
                .userId(1L)
                .token("expired-rt")
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();
        given(refreshTokenRepository.findByToken("expired-rt")).willReturn(Optional.of(rt));

        assertThatThrownBy(() -> authService.refresh("expired-rt"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.EXPIRED_TOKEN));
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 토큰 없음")
    void refresh_notFound() {
        given(refreshTokenRepository.findByToken("unknown")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("unknown"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_NOT_FOUND));
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() {
        RefreshToken rt = RefreshToken.builder().userId(1L).token("rt").expiresAt(LocalDateTime.now().plusDays(7)).build();
        given(refreshTokenRepository.findByToken("rt")).willReturn(Optional.of(rt));

        authService.logout("rt");

        verify(refreshTokenRepository).delete(rt);
    }

    @Test
    @DisplayName("사용자 소프트 삭제")
    void softDelete() {
        User user = User.builder().email("test@example.com").password("encoded").nickname("테스터").build();
        assertThat(user.isDeleted()).isFalse();

        user.softDelete();

        assertThat(user.isDeleted()).isTrue();
        assertThat(user.getDeletedAt()).isNotNull();
    }
}
