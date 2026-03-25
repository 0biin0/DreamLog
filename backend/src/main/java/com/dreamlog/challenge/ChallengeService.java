package com.dreamlog.challenge;

import com.dreamlog.challenge.dto.ChallengeCreateRequest;
import com.dreamlog.challenge.dto.ChallengeResponse;
import com.dreamlog.challenge.dto.ParticipantProgressResponse;
import com.dreamlog.global.exception.BusinessException;
import com.dreamlog.global.exception.ErrorCode;
import com.dreamlog.global.util.KstDateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipantRepository participantRepository;
    private final ChallengeDailyLogRepository dailyLogRepository;

    @Transactional
    public ChallengeResponse create(Long userId, ChallengeCreateRequest request) {
        Challenge challenge = Challenge.builder()
                .creatorId(userId)
                .title(request.title())
                .description(request.description())
                .durationDays(request.durationDays())
                .startDate(request.startDate())
                .maxParticipants(request.maxParticipants() != null ? request.maxParticipants() : 50)
                .failThreshold(request.failThreshold())
                .build();

        challengeRepository.save(challenge);

        // 생성자 자동 참가
        ChallengeParticipant participant = ChallengeParticipant.builder()
                .challengeId(challenge.getId())
                .userId(userId)
                .build();
        participantRepository.save(participant);

        return ChallengeResponse.from(challenge, 1, KstDateUtil.todayKst());
    }

    public List<ChallengeResponse> getJoinableChallenges() {
        LocalDate today = KstDateUtil.todayKst();
        return challengeRepository.findJoinableChallenges(today).stream()
                .map(c -> ChallengeResponse.from(c, participantRepository.countByChallengeId(c.getId()), today))
                .toList();
    }

    public ChallengeResponse getChallenge(Long challengeId) {
        LocalDate today = KstDateUtil.todayKst();
        Challenge challenge = findChallenge(challengeId);
        int count = participantRepository.countByChallengeId(challengeId);
        return ChallengeResponse.from(challenge, count, today);
    }

    @Transactional
    public void join(Long userId, Long challengeId) {
        Challenge challenge = findChallenge(challengeId);
        LocalDate today = KstDateUtil.todayKst();

        if (!challenge.isJoinable(today)) {
            throw new BusinessException(ErrorCode.CHALLENGE_NOT_JOINABLE);
        }

        if (participantRepository.existsByChallengeIdAndUserId(challengeId, userId)) {
            throw new BusinessException(ErrorCode.CHALLENGE_ALREADY_JOINED);
        }

        int count = participantRepository.countByChallengeId(challengeId);
        if (count >= challenge.getMaxParticipants()) {
            throw new BusinessException(ErrorCode.CHALLENGE_FULL);
        }

        ChallengeParticipant participant = ChallengeParticipant.builder()
                .challengeId(challengeId)
                .userId(userId)
                .build();
        participantRepository.save(participant);
    }

    public ParticipantProgressResponse getProgress(Long userId, Long challengeId) {
        Challenge challenge = findChallenge(challengeId);
        ChallengeParticipant participant = participantRepository.findByChallengeIdAndUserId(challengeId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_JOINED));

        List<ChallengeDailyLog> logs = dailyLogRepository.findByParticipantIdOrderByLogDateAsc(participant.getId());

        return ParticipantProgressResponse.from(participant, challenge.getMaxMissedDays(),
                challenge.getDurationDays(), logs);
    }

    public List<ChallengeResponse> getMyChallenges(Long userId) {
        LocalDate today = KstDateUtil.todayKst();
        List<ChallengeParticipant> participants = participantRepository.findByUserId(userId);

        return participants.stream()
                .map(cp -> {
                    Challenge c = findChallenge(cp.getChallengeId());
                    int count = participantRepository.countByChallengeId(c.getId());
                    return ChallengeResponse.from(c, count, today);
                })
                .toList();
    }

    private Challenge findChallenge(Long challengeId) {
        return challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));
    }
}
