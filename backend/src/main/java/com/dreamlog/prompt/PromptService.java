package com.dreamlog.prompt;

import com.dreamlog.prompt.dto.AiPromptRequest;
import com.dreamlog.prompt.dto.PromptResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromptService {

    private final PromptTemplateRepository promptTemplateRepository;

    @Value("${openai.api-key:}")
    private String openaiApiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String openaiModel;

    public PromptResponse getRandomPrompt(String category) {
        Optional<PromptTemplate> template;

        if (category != null && !category.isBlank()) {
            template = promptTemplateRepository.findRandomByCategory(category.toUpperCase());
        } else {
            template = promptTemplateRepository.findRandom();
        }

        return template.map(PromptResponse::from)
                .orElse(new PromptResponse(null, PromptCategory.RANDOM,
                        "미래의 나에게 하고 싶은 말을 자유롭게 적어보세요."));
    }

    public String getAiPrompt(AiPromptRequest request) {
        if (openaiApiKey == null || openaiApiKey.isBlank()) {
            return getFallbackPrompt(request);
        }

        try {
            return callOpenAi(request);
        } catch (Exception e) {
            log.warn("OpenAI API 호출 실패, 폴백 프롬프트 사용: {}", e.getMessage());
            return getFallbackPrompt(request);
        }
    }

    private String callOpenAi(AiPromptRequest request) {
        RestTemplate restTemplate = new RestTemplate();

        String systemPrompt = "당신은 미래 일기 작성을 돕는 도우미입니다. " +
                "사용자의 목표와 기분을 바탕으로, 미래의 자신에게 보내는 편지를 시작할 수 있는 " +
                "따뜻하고 구체적인 프롬프트를 1~2문장으로 생성해주세요. 한국어로 답변하세요.";

        String userMessage = String.format("목표: %s\n기분: %s",
                request.goal() != null ? request.goal() : "없음",
                request.mood() != null ? request.mood() : "보통");

        Map<String, Object> body = Map.of(
                "model", openaiModel,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "max_tokens", 150,
                "temperature", 0.8
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.openai.com/v1/chat/completions",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        Map responseBody = response.getBody();
        if (responseBody != null) {
            List<Map> choices = (List<Map>) responseBody.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map message = (Map) choices.get(0).get("message");
                return (String) message.get("content");
            }
        }

        return getFallbackPrompt(request);
    }

    private String getFallbackPrompt(AiPromptRequest request) {
        if (request.goal() != null && !request.goal().isBlank()) {
            return String.format("'%s'라는 목표를 이룬 미래의 나에게, 오늘의 내가 하고 싶은 말은?", request.goal());
        }
        return "1년 후의 나에게 지금 가장 하고 싶은 이야기를 적어보세요.";
    }
}
