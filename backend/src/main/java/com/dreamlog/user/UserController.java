package com.dreamlog.user;

import com.dreamlog.global.common.ApiResponse;
import com.dreamlog.user.dto.UserResponse;
import com.dreamlog.user.dto.UserUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.ok(userService.getUser(userId));
    }

    @PatchMapping("/me")
    public ApiResponse<UserResponse> updateMe(@AuthenticationPrincipal UserDetails userDetails,
                                               @Valid @RequestBody UserUpdateRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.ok(userService.updateUser(userId, request));
    }

    @DeleteMapping("/me")
    public ApiResponse<Void> deleteMe(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        userService.deleteUser(userId);
        return ApiResponse.ok();
    }
}
