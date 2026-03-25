package com.dreamlog.diary;

import com.dreamlog.diary.dto.DiaryCreateRequest;
import com.dreamlog.diary.dto.DiaryResponse;
import com.dreamlog.diary.dto.DiaryUpdateRequest;
import com.dreamlog.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    public ApiResponse<DiaryResponse> create(@AuthenticationPrincipal UserDetails userDetails,
                                              @Valid @RequestBody DiaryCreateRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.ok(diaryService.create(userId, request));
    }

    @GetMapping
    public ApiResponse<Page<DiaryResponse>> getMyDiaries(@AuthenticationPrincipal UserDetails userDetails,
                                                          @PageableDefault(size = 10) Pageable pageable) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.ok(diaryService.getMyDiaries(userId, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<DiaryResponse> getDiary(@AuthenticationPrincipal UserDetails userDetails,
                                                @PathVariable Long id) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.ok(diaryService.getDiary(userId, id));
    }

    @PatchMapping("/{id}")
    public ApiResponse<DiaryResponse> update(@AuthenticationPrincipal UserDetails userDetails,
                                              @PathVariable Long id,
                                              @Valid @RequestBody DiaryUpdateRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.ok(diaryService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal UserDetails userDetails,
                                     @PathVariable Long id) {
        Long userId = Long.parseLong(userDetails.getUsername());
        diaryService.delete(userId, id);
        return ApiResponse.ok();
    }

    @GetMapping("/arrived")
    public ApiResponse<List<DiaryResponse>> getArrivedDiaries(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.ok(diaryService.getArrivedDiaries(userId));
    }
}
