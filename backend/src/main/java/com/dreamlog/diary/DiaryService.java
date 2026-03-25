package com.dreamlog.diary;

import com.dreamlog.diary.dto.DiaryCreateRequest;
import com.dreamlog.diary.dto.DiaryResponse;
import com.dreamlog.diary.dto.DiaryUpdateRequest;
import com.dreamlog.global.exception.BusinessException;
import com.dreamlog.global.exception.ErrorCode;
import com.dreamlog.global.util.EncryptionUtil;
import com.dreamlog.global.util.KstDateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final EncryptionUtil encryptionUtil;

    @Transactional
    public DiaryResponse create(Long userId, DiaryCreateRequest request) {
        LocalDate today = KstDateUtil.todayKst();
        LocalDateTime now = KstDateUtil.nowKst();

        String encryptedContent = encryptionUtil.encrypt(request.content());

        Diary diary = Diary.builder()
                .userId(userId)
                .title(request.title())
                .content(encryptedContent)
                .unlockDate(request.unlockDate())
                .editDeadline(now.plusHours(24))
                .writtenDate(today)
                .build();

        diaryRepository.save(diary);

        return DiaryResponse.from(diary, request.content(), today, now);
    }

    public Page<DiaryResponse> getMyDiaries(Long userId, Pageable pageable) {
        LocalDate today = KstDateUtil.todayKst();
        LocalDateTime now = KstDateUtil.nowKst();

        return diaryRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId, pageable)
                .map(diary -> DiaryResponse.listItem(diary, today, now));
    }

    public DiaryResponse getDiary(Long userId, Long diaryId) {
        LocalDate today = KstDateUtil.todayKst();
        LocalDateTime now = KstDateUtil.nowKst();

        Diary diary = findDiary(userId, diaryId);

        String decryptedContent = null;
        if (!diary.isLocked(today)) {
            decryptedContent = encryptionUtil.decrypt(diary.getContent());
        }

        return DiaryResponse.from(diary, decryptedContent, today, now);
    }

    @Transactional
    public DiaryResponse update(Long userId, Long diaryId, DiaryUpdateRequest request) {
        LocalDate today = KstDateUtil.todayKst();
        LocalDateTime now = KstDateUtil.nowKst();

        Diary diary = findDiary(userId, diaryId);

        if (!diary.isEditable(now)) {
            throw new BusinessException(ErrorCode.DIARY_NOT_EDITABLE);
        }

        String encryptedContent = request.content() != null
                ? encryptionUtil.encrypt(request.content())
                : null;

        diary.update(request.title(), encryptedContent);

        String decryptedContent = diary.isLocked(today) ? null : request.content();
        return DiaryResponse.from(diary, decryptedContent, today, now);
    }

    @Transactional
    public void delete(Long userId, Long diaryId) {
        Diary diary = findDiary(userId, diaryId);
        diary.softDelete();
    }

    public List<DiaryResponse> getArrivedDiaries(Long userId) {
        LocalDate today = KstDateUtil.todayKst();
        LocalDateTime now = KstDateUtil.nowKst();

        return diaryRepository.findArrivedDiaries(userId, today).stream()
                .map(diary -> {
                    String decrypted = encryptionUtil.decrypt(diary.getContent());
                    return DiaryResponse.from(diary, decrypted, today, now);
                })
                .toList();
    }

    public boolean hasWrittenToday(Long userId) {
        return diaryRepository.existsByUserIdAndWrittenDate(userId, KstDateUtil.todayKst());
    }

    private Diary findDiary(Long userId, Long diaryId) {
        return diaryRepository.findByIdAndUserIdAndDeletedAtIsNull(diaryId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));
    }
}
