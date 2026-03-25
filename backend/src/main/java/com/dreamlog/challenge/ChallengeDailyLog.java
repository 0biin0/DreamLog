package com.dreamlog.challenge;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "challenge_daily_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeDailyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "participant_id", nullable = false)
    private Long participantId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(nullable = false)
    private boolean achieved;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    @Builder
    public ChallengeDailyLog(Long participantId, LocalDate logDate, boolean achieved) {
        this.participantId = participantId;
        this.logDate = logDate;
        this.achieved = achieved;
        this.checkedAt = LocalDateTime.now();
    }
}
