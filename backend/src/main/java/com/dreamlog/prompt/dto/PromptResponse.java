package com.dreamlog.prompt.dto;

import com.dreamlog.prompt.PromptCategory;
import com.dreamlog.prompt.PromptTemplate;

public record PromptResponse(
        Long id,
        PromptCategory category,
        String content
) {
    public static PromptResponse from(PromptTemplate template) {
        return new PromptResponse(template.getId(), template.getCategory(), template.getContentKo());
    }
}
