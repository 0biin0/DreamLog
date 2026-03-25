package com.dreamlog.challenge;

import com.dreamlog.challenge.dto.ChallengeCreateRequest;
import com.dreamlog.challenge.dto.ChallengeResponse;
import com.dreamlog.global.exception.BusinessException;
import com.dreamlog.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {

    @InjectMocks
    private ChallengeService challengeService;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private ChallengeParticipantRepository participantRepository;

    @Mock
    private ChallengeDailyLogRepository dailyLogRepository;

    @Test
    @DisplayName("챌린지 생성 성공 + 생성자 자동 참가")
    void create_success() {
        ChallengeCreateRequest request = new ChallengeCreateRequest(
                "7일 미래일기 챌린지", "매일 미래일기 쓰기",
                7, LocalDate.now().plusDays(1), 50, new BigDecimal("0.15")
        );
        given(challengeRepository.save(any(Challenge.class))).willAnswer(inv -> {
            Challenge c = inv.getArgument(0);
            try {
                var idField = Challenge.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(c, 1L);
            } catch (Exception ignored) {}
            return c;
        });
        given(participantRepository.save(any(ChallengeParticipant.class))).willAnswer(inv -> inv.getArgument(0));
        given(participantRepository.countByChallengeId(1L)).willReturn(1);

        ChallengeResponse result = challengeService.create(1L, request);

        assertThat(result.title()).isEqualTo("7일 미래일기 챌린지");
        assertThat(result.currentParticipants()).isEqualTo(1);
        verify(participantRepository).save(any(ChallengeParticipant.class));
    }

    @Test
    @DisplayName("챌린지 참가 실패 - 이미 참가")
    void join_alreadyJoined() {
        Challenge challenge = Challenge.builder()
                .creatorId(1L).title("test").durationDays(7)
                .startDate(LocalDate.now().plusDays(1)).build();
        try {
            var f = Challenge.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(challenge, 1L);
        } catch (Exception ignored) {}

        given(challengeRepository.findById(1L)).willReturn(Optional.of(challenge));
        given(participantRepository.existsByChallengeIdAndUserId(1L, 2L)).willReturn(true);

        assertThatThrownBy(() -> challengeService.join(2L, 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.CHALLENGE_ALREADY_JOINED));
    }

    @Test
    @DisplayName("챌린지 참가 실패 - 인원 초과")
    void join_full() {
        Challenge challenge = Challenge.builder()
                .creatorId(1L).title("test").durationDays(7)
                .startDate(LocalDate.now().plusDays(1)).maxParticipants(2).build();
        try {
            var f = Challenge.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(challenge, 1L);
        } catch (Exception ignored) {}

        given(challengeRepository.findById(1L)).willReturn(Optional.of(challenge));
        given(participantRepository.existsByChallengeIdAndUserId(1L, 3L)).willReturn(false);
        given(participantRepository.countByChallengeId(1L)).willReturn(2);

        assertThatThrownBy(() -> challengeService.join(3L, 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.CHALLENGE_FULL));
    }
}
