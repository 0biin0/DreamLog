package com.dreamlog.challenge.dto;

import com.dreamlog.challenge.ChallengeDailyLog;
import com.dreamlog.challenge.ChallengeParticipant;
import com.dreamlog.challenge.ParticipantStatus;

import java.util.List;

public record ParticipantProgressResponse(
        Long participantId,
        ParticipantStatus status,
        int missedCount,
        int maxMissedDays,
        int totalDays,
        int achievedDays,
        double progressPercent,
        List<DailyLogEntry> dailyLogs
) {
    public record DailyLogEntry(String date, boolean achieved) {}

    public static ParticipantProgressResponse from(ChallengeParticipant cp, int maxMissed, int totalDays,
                                                     List<ChallengeDailyLog> logs) {
        int achieved = (int) logs.stream().filter(ChallengeDailyLog::isAchieved).count();
        int checkedDays = logs.size();
        double progress = totalDays > 0 ? (double) checkedDays / totalDays * 100 : 0;

        List<DailyLogEntry> entries = logs.stream()
                .map(l -> new DailyLogEntry(l.getLogDate().toString(), l.isAchieved()))
                .toList();

        return new ParticipantProgressResponse(
                cp.getId(), cp.getStatus(), cp.getMissedCount(),
                maxMissed, totalDays, achieved, Math.round(progress * 10) / 10.0, entries
        );
    }
}
