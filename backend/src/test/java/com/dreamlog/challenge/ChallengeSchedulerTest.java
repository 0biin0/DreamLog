package com.dreamlog.challenge;

import com.dreamlog.diary.DiaryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeSchedulerTest {

    @InjectMocks
    private ChallengeScheduler challengeScheduler;

    @Mock
    private ChallengeParticipantRepository participantRepository;

    @Mock
    private ChallengeDailyLogRepository dailyLogRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private DiaryRepository diaryRepository;

    private Challenge createChallenge(LocalDate startDate, int days) {
        Challenge c = Challenge.builder()
                .creatorId(1L).title("test").durationDays(days)
                .startDate(startDate).failThreshold(new BigDecimal("0.15")).build();
        try {
            var f = Challenge.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(c, 1L);
        } catch (Exception ignored) {}
        return c;
    }

    private ChallengeParticipant createParticipant(Long id, Long challengeId, Long userId) {
        ChallengeParticipant cp = ChallengeParticipant.builder().challengeId(challengeId).userId(userId).build();
        try {
            var f = ChallengeParticipant.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(cp, id);
        } catch (Exception ignored) {}
        return cp;
    }

    @Test
    @DisplayName("일기 작성 시 achieved=true 기록")
    void checkDaily_achieved() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Challenge challenge = createChallenge(yesterday.minusDays(2), 7);
        ChallengeParticipant cp = createParticipant(1L, 1L, 10L);

        given(participantRepository.findAllActiveInRunningChallenges(any())).willReturn(List.of(cp));
        given(challengeRepository.findById(1L)).willReturn(Optional.of(challenge));
        given(dailyLogRepository.findLastCheckedDate(1L)).willReturn(Optional.of(yesterday.minusDays(1)));
        given(dailyLogRepository.existsByParticipantIdAndLogDate(1L, yesterday)).willReturn(false);
        given(diaryRepository.existsByUserIdAndWrittenDate(10L, yesterday)).willReturn(true);

        challengeScheduler.checkDailyProgressForDate(yesterday);

        verify(dailyLogRepository).save(argThat(log -> log.isAchieved() && log.getLogDate().equals(yesterday)));
    }

    @Test
    @DisplayName("일기 미작성 시 missedCount 증가")
    void checkDaily_missed() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Challenge challenge = createChallenge(yesterday.minusDays(2), 7);
        ChallengeParticipant cp = createParticipant(2L, 1L, 20L);

        given(participantRepository.findAllActiveInRunningChallenges(any())).willReturn(List.of(cp));
        given(challengeRepository.findById(1L)).willReturn(Optional.of(challenge));
        given(dailyLogRepository.findLastCheckedDate(2L)).willReturn(Optional.of(yesterday.minusDays(1)));
        given(dailyLogRepository.existsByParticipantIdAndLogDate(2L, yesterday)).willReturn(false);
        given(diaryRepository.existsByUserIdAndWrittenDate(20L, yesterday)).willReturn(false);

        challengeScheduler.checkDailyProgressForDate(yesterday);

        assertThat(cp.getMissedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("missed > maxMissed 시 FAILED")
    void checkDaily_fail() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        // 7일 * 0.15 = floor(1.05) = 1일 허용. 이미 missed=1이면 다음 miss에서 fail
        Challenge challenge = createChallenge(yesterday.minusDays(5), 7);
        ChallengeParticipant cp = createParticipant(3L, 1L, 30L);
        // 이미 1번 missed
        cp.incrementMissed();

        given(participantRepository.findAllActiveInRunningChallenges(any())).willReturn(List.of(cp));
        given(challengeRepository.findById(1L)).willReturn(Optional.of(challenge));
        given(dailyLogRepository.findLastCheckedDate(3L)).willReturn(Optional.of(yesterday.minusDays(1)));
        given(dailyLogRepository.existsByParticipantIdAndLogDate(3L, yesterday)).willReturn(false);
        given(diaryRepository.existsByUserIdAndWrittenDate(30L, yesterday)).willReturn(false);

        challengeScheduler.checkDailyProgressForDate(yesterday);

        assertThat(cp.getStatus()).isEqualTo(ParticipantStatus.FAILED);
    }

    @Test
    @DisplayName("보상 로직 - 누락된 날짜 소급 처리")
    void checkDaily_compensation() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(4);
        Challenge challenge = createChallenge(startDate, 7);
        ChallengeParticipant cp = createParticipant(4L, 1L, 40L);

        // 마지막 체크가 2일 전 → 어제+그저께 2일치 소급 처리
        given(participantRepository.findAllActiveInRunningChallenges(any())).willReturn(List.of(cp));
        given(challengeRepository.findById(1L)).willReturn(Optional.of(challenge));
        given(dailyLogRepository.findLastCheckedDate(4L)).willReturn(Optional.of(today.minusDays(3)));
        given(dailyLogRepository.existsByParticipantIdAndLogDate(eq(4L), any())).willReturn(false);
        given(diaryRepository.existsByUserIdAndWrittenDate(eq(40L), any())).willReturn(true);

        challengeScheduler.checkDailyProgressForDate(today.minusDays(1));

        // 2일치 (today-2, today-1) 저장
        verify(dailyLogRepository, times(2)).save(any(ChallengeDailyLog.class));
    }

    @Test
    @DisplayName("챌린지 종료 시 ACTIVE → COMPLETED")
    void checkDaily_complete() {
        LocalDate endDate = LocalDate.now().minusDays(1);
        Challenge challenge = createChallenge(endDate.minusDays(6), 7);
        ChallengeParticipant cp = createParticipant(5L, 1L, 50L);

        given(participantRepository.findAllActiveInRunningChallenges(any())).willReturn(List.of(cp));
        given(challengeRepository.findById(1L)).willReturn(Optional.of(challenge));
        given(dailyLogRepository.findLastCheckedDate(5L)).willReturn(Optional.of(endDate.minusDays(1)));
        given(dailyLogRepository.existsByParticipantIdAndLogDate(5L, endDate)).willReturn(false);
        given(diaryRepository.existsByUserIdAndWrittenDate(50L, endDate)).willReturn(true);

        challengeScheduler.checkDailyProgressForDate(endDate);

        assertThat(cp.getStatus()).isEqualTo(ParticipantStatus.COMPLETED);
    }
}
