package com.dreamlog.challenge.dto;

import com.dreamlog.challenge.Challenge;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ChallengeResponse(
        Long id,
        Long creatorId,
        String title,
        String description,
        int durationDays,
        LocalDate startDate,
        LocalDate endDate,
        int maxParticipants,
        int currentParticipants,
        BigDecimal failThreshold,
        int maxMissedDays,
        String status // UPCOMING, ACTIVE, ENDED
) {
    public static ChallengeResponse from(Challenge c, int currentParticipants, LocalDate today) {
        String status;
        if (today.isBefore(c.getStartDate())) status = "UPCOMING";
        else if (c.isEnded(today)) status = "ENDED";
        else status = "ACTIVE";

        return new ChallengeResponse(
                c.getId(), c.getCreatorId(), c.getTitle(), c.getDescription(),
                c.getDurationDays(), c.getStartDate(), c.getEndDate(),
                c.getMaxParticipants(), currentParticipants, c.getFailThreshold(),
                c.getMaxMissedDays(), status
        );
    }
}
