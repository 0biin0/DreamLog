package com.dreamlog.diary;

import com.dreamlog.diary.dto.DiaryCreateRequest;
import com.dreamlog.diary.dto.DiaryResponse;
import com.dreamlog.diary.dto.DiaryUpdateRequest;
import com.dreamlog.global.exception.BusinessException;
import com.dreamlog.global.exception.ErrorCode;
import com.dreamlog.global.util.EncryptionUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {

    @InjectMocks
    private DiaryService diaryService;

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    private Diary createDiary(Long id, Long userId, LocalDate unlockDate, LocalDateTime editDeadline) {
        Diary diary = Diary.builder()
                .userId(userId)
                .title("테스트 일기")
                .content("encrypted-content")
                .unlockDate(unlockDate)
                .editDeadline(editDeadline)
                .writtenDate(LocalDate.now())
                .build();
        // Reflection to set id for testing
        try {
            var idField = Diary.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(diary, id);
        } catch (Exception ignored) {}
        return diary;
    }

    @Test
    @DisplayName("일기 작성 성공")
    void create_success() {
        DiaryCreateRequest request = new DiaryCreateRequest("제목", "내용", LocalDate.now().plusDays(30));
        given(encryptionUtil.encrypt("내용")).willReturn("encrypted");
        given(diaryRepository.save(any(Diary.class))).willAnswer(inv -> inv.getArgument(0));

        DiaryResponse result = diaryService.create(1L, request);

        assertThat(result.title()).isEqualTo("제목");
        assertThat(result.locked()).isTrue();
        verify(diaryRepository).save(any(Diary.class));
    }

    @Test
    @DisplayName("일기 목록 조회")
    void getMyDiaries() {
        Diary diary = createDiary(1L, 1L, LocalDate.now().plusDays(10), LocalDateTime.now().plusHours(24));
        given(diaryRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(eq(1L), any()))
                .willReturn(new PageImpl<>(List.of(diary)));

        Page<DiaryResponse> result = diaryService.getMyDiaries(1L, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).content()).isNull(); // 목록에서는 content 없음
    }

    @Test
    @DisplayName("잠긴 일기 상세 조회 - content null")
    void getDiary_locked() {
        Diary diary = createDiary(1L, 1L, LocalDate.now().plusDays(30), LocalDateTime.now().plusHours(24));
        given(diaryRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, 1L)).willReturn(Optional.of(diary));

        DiaryResponse result = diaryService.getDiary(1L, 1L);

        assertThat(result.locked()).isTrue();
        assertThat(result.content()).isNull();
        assertThat(result.daysUntilUnlock()).isGreaterThan(0);
    }

    @Test
    @DisplayName("열린 일기 상세 조회 - content 복호화")
    void getDiary_unlocked() {
        Diary diary = createDiary(1L, 1L, LocalDate.now().minusDays(1), LocalDateTime.now().minusHours(20));
        given(diaryRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, 1L)).willReturn(Optional.of(diary));
        given(encryptionUtil.decrypt("encrypted-content")).willReturn("복호화된 내용");

        DiaryResponse result = diaryService.getDiary(1L, 1L);

        assertThat(result.locked()).isFalse();
        assertThat(result.content()).isEqualTo("복호화된 내용");
    }

    @Test
    @DisplayName("일기 없음")
    void getDiary_notFound() {
        given(diaryRepository.findByIdAndUserIdAndDeletedAtIsNull(99L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> diaryService.getDiary(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.DIARY_NOT_FOUND));
    }

    @Test
    @DisplayName("일기 수정 성공 (24시간 내)")
    void update_success() {
        Diary diary = createDiary(1L, 1L, LocalDate.now().plusDays(30), LocalDateTime.now().plusHours(12));
        given(diaryRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, 1L)).willReturn(Optional.of(diary));
        given(encryptionUtil.encrypt("수정된 내용")).willReturn("encrypted-updated");

        DiaryResponse result = diaryService.update(1L, 1L, new DiaryUpdateRequest("수정 제목", "수정된 내용"));

        assertThat(result.title()).isEqualTo("수정 제목");
    }

    @Test
    @DisplayName("일기 수정 실패 - 24시간 초과")
    void update_expired() {
        Diary diary = createDiary(1L, 1L, LocalDate.now().plusDays(30), LocalDateTime.now().minusHours(1));
        given(diaryRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, 1L)).willReturn(Optional.of(diary));

        assertThatThrownBy(() -> diaryService.update(1L, 1L, new DiaryUpdateRequest("수정", "수정")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.DIARY_NOT_EDITABLE));
    }

    @Test
    @DisplayName("일기 삭제 성공")
    void delete_success() {
        Diary diary = createDiary(1L, 1L, LocalDate.now().plusDays(30), LocalDateTime.now().plusHours(24));
        given(diaryRepository.findByIdAndUserIdAndDeletedAtIsNull(1L, 1L)).willReturn(Optional.of(diary));

        diaryService.delete(1L, 1L);

        assertThat(diary.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("도착한 편지 조회")
    void getArrivedDiaries() {
        Diary diary = createDiary(1L, 1L, LocalDate.now().minusDays(1), LocalDateTime.now().minusDays(30));
        given(diaryRepository.findArrivedDiaries(eq(1L), any(LocalDate.class))).willReturn(List.of(diary));
        given(encryptionUtil.decrypt("encrypted-content")).willReturn("편지 내용");

        List<DiaryResponse> result = diaryService.getArrivedDiaries(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).content()).isEqualTo("편지 내용");
        assertThat(result.get(0).locked()).isFalse();
    }

    @Test
    @DisplayName("오늘 작성 여부 확인")
    void hasWrittenToday() {
        given(diaryRepository.existsByUserIdAndWrittenDate(eq(1L), any(LocalDate.class))).willReturn(true);

        assertThat(diaryService.hasWrittenToday(1L)).isTrue();
    }
}
