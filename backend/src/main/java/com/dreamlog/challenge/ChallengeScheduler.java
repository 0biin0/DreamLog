package com.dreamlog.challenge;

import com.dreamlog.diary.DiaryRepository;
import com.dreamlog.global.util.KstDateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeScheduler {

    private final ChallengeParticipantRepository participantRepository;
    private final ChallengeDailyLogRepository dailyLogRepository;
    private final ChallengeRepository challengeRepository;
    private final DiaryRepository diaryRepository;

    /**
     * 매일 KST 00:05 실행.
     * 어제 날짜 기준으로 챌린지 참가자의 일기 작성 여부를 체크하고,
     * 누락된 날짜를 소급 처리하는 보상 로직 포함.
     */
    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void checkDailyProgress() {
        LocalDate today = KstDateUtil.todayKst();
        LocalDate yesterday = today.minusDays(1);

        log.info("챌린지 일일 정산 시작: {}", yesterday);

        List<ChallengeParticipant> activeParticipants =
                participantRepository.findAllActiveInRunningChallenges(today);

        for (ChallengeParticipant participant : activeParticipants) {
            processParticipant(participant, yesterday);
        }

        log.info("챌린지 일일 정산 완료: {} 참가자 처리", activeParticipants.size());
    }

    /**
     * 수동 실행용 (테스트, 보상 로직).
     */
    @Transactional
    public void checkDailyProgressForDate(LocalDate targetDate) {
        List<ChallengeParticipant> activeParticipants =
                participantRepository.findAllActiveInRunningChallenges(targetDate);

        for (ChallengeParticipant participant : activeParticipants) {
            processParticipant(participant, targetDate);
        }
    }

    private void processParticipant(ChallengeParticipant participant, LocalDate targetDate) {
        Challenge challenge = challengeRepository.findById(participant.getChallengeId()).orElse(null);
        if (challenge == null) return;

        // 보상 로직: 마지막 체크 날짜 이후 모든 미체크 날짜를 소급 처리
        Optional<LocalDate> lastChecked = dailyLogRepository.findLastCheckedDate(participant.getId());
        LocalDate startFrom = lastChecked.map(d -> d.plusDays(1))
                .orElse(challenge.getStartDate());

        // startFrom부터 targetDate까지 순회
        LocalDate current = startFrom;
        while (!current.isAfter(targetDate) && !current.isAfter(challenge.getEndDate())) {
            if (!dailyLogRepository.existsByParticipantIdAndLogDate(participant.getId(), current)) {
                boolean achieved = diaryRepository.existsByUserIdAndWrittenDate(
                        participant.getUserId(), current);

                ChallengeDailyLog logEntry = ChallengeDailyLog.builder()
                        .participantId(participant.getId())
                        .logDate(current)
                        .achieved(achieved)
                        .build();
                dailyLogRepository.save(logEntry);

                if (!achieved) {
                    participant.incrementMissed();

                    if (participant.getMissedCount() > challenge.getMaxMissedDays()) {
                        participant.fail();
                        log.info("참가자 {} FAILED (missed: {}/{})", participant.getId(),
                                participant.getMissedCount(), challenge.getMaxMissedDays());
                        return;
                    }
                }
            }
            current = current.plusDays(1);
        }

        // 마지막 날 체크: 챌린지 종료 && 아직 ACTIVE면 COMPLETED
        if (challenge.isEnded(targetDate.plusDays(1)) && participant.isActive()) {
            participant.complete();
            log.info("참가자 {} COMPLETED", participant.getId());
        }
    }
}
