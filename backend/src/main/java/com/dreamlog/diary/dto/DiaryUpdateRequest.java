package com.dreamlog.diary.dto;

import jakarta.validation.constraints.Size;

public record DiaryUpdateRequest(
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,
        String content
) {}
