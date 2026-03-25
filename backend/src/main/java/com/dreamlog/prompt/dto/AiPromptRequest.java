package com.dreamlog.prompt.dto;

import jakarta.validation.constraints.Size;

public record AiPromptRequest(
        @Size(max = 200, message = "목표는 200자 이하여야 합니다.")
        String goal,
        String mood
) {}
