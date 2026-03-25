package com.dreamlog.challenge;

import com.dreamlog.challenge.dto.ChallengeCreateRequest;
import com.dreamlog.challenge.dto.ChallengeResponse;
import com.dreamlog.challenge.dto.ParticipantProgressResponse;
import com.dreamlog.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @PostMapping
    public ApiResponse<ChallengeResponse> create(@AuthenticationPrincipal UserDetails userDetails,
                                                  @Valid @RequestBody ChallengeCreateRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.ok(challengeService.create(userId, request));
    }

    @GetMapping
    public ApiResponse<List<ChallengeResponse>> getJoinable() {
        return ApiResponse.ok(challengeService.getJoinableChallenges());
    }

    @GetMapping("/{id}")
    public ApiResponse<ChallengeResponse> getChallenge(@PathVariable Long id) {
        return ApiResponse.ok(challengeService.getChallenge(id));
    }

    @PostMapping("/{id}/join")
    public ApiResponse<Void> join(@AuthenticationPrincipal UserDetails userDetails,
                                   @PathVariable Long id) {
        Long userId = Long.parseLong(userDetails.getUsername());
        challengeService.join(userId, id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/progress")
    public ApiResponse<ParticipantProgressResponse> getProgress(@AuthenticationPrincipal UserDetails userDetails,
                                                                 @PathVariable Long id) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.ok(challengeService.getProgress(userId, id));
    }

    @GetMapping("/my")
    public ApiResponse<List<ChallengeResponse>> getMyChallenges(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.ok(challengeService.getMyChallenges(userId));
    }
}
