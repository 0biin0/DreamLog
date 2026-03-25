package com.dreamlog.diary;

import com.dreamlog.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "diaries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diary extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "unlock_date", nullable = false)
    private LocalDate unlockDate;

    @Column(name = "edit_deadline", nullable = false)
    private LocalDateTime editDeadline;

    @Column(name = "written_date", nullable = false)
    private LocalDate writtenDate;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Diary(Long userId, String title, String content, LocalDate unlockDate,
                 LocalDateTime editDeadline, LocalDate writtenDate) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.unlockDate = unlockDate;
        this.editDeadline = editDeadline;
        this.writtenDate = writtenDate;
    }

    public void update(String title, String content) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isLocked(LocalDate today) {
        return today.isBefore(unlockDate);
    }

    public boolean isEditable(LocalDateTime now) {
        return now.isBefore(editDeadline) && deletedAt == null;
    }
}
