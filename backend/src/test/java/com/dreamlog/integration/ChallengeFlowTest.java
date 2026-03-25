package com.dreamlog.integration;

import com.dreamlog.auth.AuthService;
import com.dreamlog.auth.dto.SignupRequest;
import com.dreamlog.challenge.ChallengeScheduler;
import com.dreamlog.challenge.ChallengeService;
import com.dreamlog.challenge.dto.ChallengeCreateRequest;
import com.dreamlog.challenge.dto.ChallengeResponse;
import com.dreamlog.challenge.dto.ParticipantProgressResponse;
import com.dreamlog.diary.DiaryService;
import com.dreamlog.diary.dto.DiaryCreateRequest;
import com.dreamlog.global.exception.BusinessException;
import com.dreamlog.global.exception.ErrorCode;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * E2E 테스트 2: 챌린지 생성 → 참가 → 일기 작성 → 진행 확인 플로우
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChallengeFlowTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private DiaryService diaryService;

    @Autowired
    private ChallengeScheduler challengeScheduler;

    @Test
    @Order(1)
    @DisplayName("전체 플로우: 챌린지 생성 → 참가 → 일기 작성 → 스케줄러 → 진행 확인")
    void fullChallengeFlow() {
        // 1. 두 사용자 가입
        Long creatorId = authService.signup(
                new SignupRequest("creator@test.com", "password123", "생성자")).user().id();
        Long joinerId = authService.signup(
                new SignupRequest("joiner@test.com", "password123", "참가자")).user().id();

        // 2. 챌린지 생성 (내일 시작, 7일)
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        ChallengeCreateRequest createReq = new ChallengeCreateRequest(
                "7일 미래일기 챌린지", "매일 미래일기 한 편 쓰기",
                7, tomorrow, 50, new BigDecimal("0.15")
        );
        ChallengeResponse challenge = challengeService.create(creatorId, createReq);

        assertThat(challenge.title()).isEqualTo("7일 미래일기 챌린지");
        assertThat(challenge.currentParticipants()).isEqualTo(1); // 생성자 자동 참가
        assertThat(challenge.maxMissedDays()).isEqualTo(1); // floor(7 * 0.15) = 1

        // 3. 두 번째 사용자 참가
        challengeService.join(joinerId, challenge.id());
        ChallengeResponse updated = challengeService.getChallenge(challenge.id());
        assertThat(updated.currentParticipants()).isEqualTo(2);

        // 4. 중복 참가 시도
        assertThatThrownBy(() -> challengeService.join(joinerId, challenge.id()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.CHALLENGE_ALREADY_JOINED));

        // 5. 내 챌린지 목록
        List<ChallengeResponse> myList = challengeService.getMyChallenges(joinerId);
        assertThat(myList).hasSize(1);
        assertThat(myList.get(0).title()).isEqualTo("7일 미래일기 챌린지");

        // 6. 참가 가능 챌린지 목록 (내일 시작이므 아직 참가 가능)
        List<ChallengeResponse> joinable = challengeService.getJoinableChallenges();
        assertThat(joinable).anyMatch(c -> c.id().equals(challenge.id()));

        // 7. 진행상황 조회
        ParticipantProgressResponse progress = challengeService.getProgress(joinerId, challenge.id());
        assertThat(progress.status().name()).isEqualTo("ACTIVE");
        assertThat(progress.missedCount()).isEqualTo(0);
        assertThat(progress.totalDays()).isEqualTo(7);
    }

    @Test
    @Order(2)
    @DisplayName("오늘 일기 작성 여부 확인")
    void todayWriteCheck() {
        Long userId = authService.signup(
                new SignupRequest("writer@test.com", "password123", "작성자")).user().id();

        assertThat(diaryService.hasWrittenToday(userId)).isFalse();

        diaryService.create(userId, new DiaryCreateRequest(
                "오늘의 일기", "내용", LocalDate.now().plusDays(30)));

        assertThat(diaryService.hasWrittenToday(userId)).isTrue();
    }
}
