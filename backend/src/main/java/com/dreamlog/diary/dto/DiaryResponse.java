package com.dreamlog.diary.dto;

import com.dreamlog.diary.Diary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record DiaryResponse(
        Long id,
        String title,
        String content,
        LocalDate unlockDate,
        LocalDate writtenDate,
        LocalDateTime editDeadline,
        boolean locked,
        long daysUntilUnlock,
        boolean editable,
        LocalDateTime createdAt
) {
    public static DiaryResponse from(Diary diary, String decryptedContent, LocalDate today, LocalDateTime now) {
        boolean locked = diary.isLocked(today);
        long daysUntil = locked ? ChronoUnit.DAYS.between(today, diary.getUnlockDate()) : 0;

        return new DiaryResponse(
                diary.getId(),
                diary.getTitle(),
                locked ? null : decryptedContent,
                diary.getUnlockDate(),
                diary.getWrittenDate(),
                diary.getEditDeadline(),
                locked,
                daysUntil,
                diary.isEditable(now),
                diary.getCreatedAt()
        );
    }

    public static DiaryResponse listItem(Diary diary, LocalDate today, LocalDateTime now) {
        boolean locked = diary.isLocked(today);
        long daysUntil = locked ? ChronoUnit.DAYS.between(today, diary.getUnlockDate()) : 0;

        return new DiaryResponse(
                diary.getId(),
                diary.getTitle(),
                null,
                diary.getUnlockDate(),
                diary.getWrittenDate(),
                diary.getEditDeadline(),
                locked,
                daysUntil,
                diary.isEditable(now),
                diary.getCreatedAt()
        );
    }
}
