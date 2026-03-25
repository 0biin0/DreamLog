package com.dreamlog.prompt;

import com.dreamlog.prompt.dto.AiPromptRequest;
import com.dreamlog.prompt.dto.PromptResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PromptServiceTest {

    @InjectMocks
    private PromptService promptService;

    @Mock
    private PromptTemplateRepository promptTemplateRepository;

    @Test
    @DisplayName("랜덤 프롬프트 조회 - DB에서 반환")
    void getRandomPrompt_fromDb() {
        PromptTemplate template = new PromptTemplate();
        try {
            var idField = PromptTemplate.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(template, 1L);
            var catField = PromptTemplate.class.getDeclaredField("category");
            catField.setAccessible(true);
            catField.set(template, PromptCategory.MOTIVATION);
            var contentField = PromptTemplate.class.getDeclaredField("contentKo");
            contentField.setAccessible(true);
            contentField.set(template, "테스트 프롬프트");
        } catch (Exception ignored) {}

        given(promptTemplateRepository.findRandom()).willReturn(Optional.of(template));

        PromptResponse result = promptService.getRandomPrompt(null);

        assertThat(result.content()).isEqualTo("테스트 프롬프트");
        assertThat(result.category()).isEqualTo(PromptCategory.MOTIVATION);
    }

    @Test
    @DisplayName("AI 프롬프트 - OpenAI 키 없을 때 폴백")
    void getAiPrompt_fallback() {
        // openaiApiKey는 빈 문자열 (기본값)
        AiPromptRequest request = new AiPromptRequest("영어 마스터", "설렘");

        String result = promptService.getAiPrompt(request);

        assertThat(result).contains("영어 마스터");
    }
}
