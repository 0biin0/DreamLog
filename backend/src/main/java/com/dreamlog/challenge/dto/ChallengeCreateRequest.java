package com.dreamlog.challenge.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ChallengeCreateRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,

        String description,

        @NotNull(message = "기간은 필수입니다.")
        @Min(value = 3, message = "최소 3일 이상이어야 합니다.")
        @Max(value = 90, message = "최대 90일까지 가능합니다.")
        Integer durationDays,

        @NotNull(message = "시작일은 필수입니다.")
        @FutureOrPresent(message = "시작일은 오늘 이후여야 합니다.")
        LocalDate startDate,

        @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
        @Max(value = 100, message = "최대 100명까지 가능합니다.")
        Integer maxParticipants,

        BigDecimal failThreshold
) {}
