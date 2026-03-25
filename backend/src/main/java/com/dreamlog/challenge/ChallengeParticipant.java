package com.dreamlog.challenge;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "challenge_participants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "challenge_id", nullable = false)
    private Long challengeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipantStatus status = ParticipantStatus.ACTIVE;

    @Column(name = "missed_count", nullable = false)
    private int missedCount = 0;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    public ChallengeParticipant(Long challengeId, Long userId) {
        this.challengeId = challengeId;
        this.userId = userId;
        this.status = ParticipantStatus.ACTIVE;
        this.missedCount = 0;
        this.joinedAt = LocalDateTime.now();
    }

    public void incrementMissed() {
        this.missedCount++;
    }

    public void fail() {
        this.status = ParticipantStatus.FAILED;
    }

    public void complete() {
        this.status = ParticipantStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == ParticipantStatus.ACTIVE;
    }
}
