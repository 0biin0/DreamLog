package com.dreamlog.challenge;

import com.dreamlog.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "challenges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "max_participants")
    private int maxParticipants = 50;

    @Column(name = "fail_threshold", nullable = false, precision = 3, scale = 2)
    private BigDecimal failThreshold = new BigDecimal("0.15");

    @Builder
    public Challenge(Long creatorId, String title, String description, int durationDays,
                     LocalDate startDate, int maxParticipants, BigDecimal failThreshold) {
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.durationDays = durationDays;
        this.startDate = startDate;
        this.endDate = startDate.plusDays(durationDays - 1);
        this.maxParticipants = maxParticipants;
        this.failThreshold = failThreshold != null ? failThreshold : new BigDecimal("0.15");
    }

    public int getMaxMissedDays() {
        return (int) Math.floor(durationDays * failThreshold.doubleValue());
    }

    public boolean isActive(LocalDate today) {
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    public boolean isEnded(LocalDate today) {
        return today.isAfter(endDate);
    }

    public boolean isJoinable(LocalDate today) {
        return !today.isAfter(startDate);
    }
}
