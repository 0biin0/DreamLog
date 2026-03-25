package com.dreamlog.prompt;

import com.dreamlog.global.common.ApiResponse;
import com.dreamlog.prompt.dto.AiPromptRequest;
import com.dreamlog.prompt.dto.PromptResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/prompts")
@RequiredArgsConstructor
public class PromptController {

    private final PromptService promptService;

    @GetMapping("/random")
    public ApiResponse<PromptResponse> getRandomPrompt(
            @RequestParam(required = false) String category) {
        return ApiResponse.ok(promptService.getRandomPrompt(category));
    }

    @PostMapping("/ai")
    public ApiResponse<Map<String, String>> getAiPrompt(@Valid @RequestBody AiPromptRequest request) {
        String prompt = promptService.getAiPrompt(request);
        return ApiResponse.ok(Map.of("prompt", prompt));
    }
}
