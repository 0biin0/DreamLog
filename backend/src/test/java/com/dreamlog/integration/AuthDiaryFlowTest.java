package com.dreamlog.integration;

import com.dreamlog.auth.AuthService;
import com.dreamlog.auth.dto.LoginRequest;
import com.dreamlog.auth.dto.SignupRequest;
import com.dreamlog.diary.DiaryService;
import com.dreamlog.diary.dto.DiaryCreateRequest;
import com.dreamlog.diary.dto.DiaryResponse;
import com.dreamlog.diary.dto.DiaryUpdateRequest;
import com.dreamlog.global.exception.BusinessException;
import com.dreamlog.global.exception.ErrorCode;
import com.dreamlog.user.UserService;
import com.dreamlog.user.dto.UserResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * E2E 테스트 1: 회원가입 → 로그인 → 일기 작성 → 조회 → 수정 → 삭제 플로우
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthDiaryFlowTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private DiaryService diaryService;

    @Autowired
    private UserService userService;

    @Test
    @Order(1)
    @DisplayName("전체 플로우: 회원가입 → 일기 작성 → 잠금 확인 → 수정 → 삭제")
    void fullFlow() {
        // 1. 회원가입
        SignupRequest signupRequest = new SignupRequest("flow@test.com", "password123", "플로우테스터");
        AuthService.AuthResult signupResult = authService.signup(signupRequest);

        assertThat(signupResult.tokenResponse().accessToken()).isNotBlank();
        assertThat(signupResult.user().email()).isEqualTo("flow@test.com");
        assertThat(signupResult.user().nickname()).isEqualTo("플로우테스터");

        Long userId = signupResult.user().id();

        // 2. 로그인
        LoginRequest loginRequest = new LoginRequest("flow@test.com", "password123");
        AuthService.AuthResult loginResult = authService.login(loginRequest);
        assertThat(loginResult.tokenResponse().accessToken()).isNotBlank();

        // 3. 내 정보 조회
        UserResponse me = userService.getUser(userId);
        assertThat(me.nickname()).isEqualTo("플로우테스터");

        // 4. 미래 일기 작성
        DiaryCreateRequest diaryRequest = new DiaryCreateRequest(
                "미래의 나에게", "1년 후의 나에게 보내는 편지입니다.",
                LocalDate.now().plusDays(365)
        );
        DiaryResponse created = diaryService.create(userId, diaryRequest);

        assertThat(created.title()).isEqualTo("미래의 나에게");
        assertThat(created.locked()).isTrue();
        assertThat(created.daysUntilUnlock()).isGreaterThan(300);
        assertThat(created.editable()).isTrue();

        // 5. 잠긴 일기 상세 조회 → content null
        DiaryResponse locked = diaryService.getDiary(userId, created.id());
        assertThat(locked.content()).isNull();
        assertThat(locked.locked()).isTrue();

        // 6. 일기 수정 (24시간 내)
        DiaryResponse updated = diaryService.update(userId, created.id(),
                new DiaryUpdateRequest("수정된 제목", "수정된 내용"));
        assertThat(updated.title()).isEqualTo("수정된 제목");

        // 7. 일기 삭제
        diaryService.delete(userId, created.id());

        // 8. 삭제된 일기 조회 → NOT FOUND
        assertThatThrownBy(() -> diaryService.getDiary(userId, created.id()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.DIARY_NOT_FOUND));

        // 9. 중복 가입 시도
        assertThatThrownBy(() -> authService.signup(signupRequest))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.DUPLICATE_EMAIL));

        // 10. 토큰 갱신
        String rt = signupResult.refreshToken();
        var refreshed = authService.refresh(rt);
        assertThat(refreshed.accessToken()).isNotBlank();
    }
}
