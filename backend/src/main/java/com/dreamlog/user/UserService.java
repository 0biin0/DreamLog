package com.dreamlog.user;

import com.dreamlog.global.exception.BusinessException;
import com.dreamlog.global.exception.ErrorCode;
import com.dreamlog.user.dto.UserResponse;
import com.dreamlog.user.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getUser(Long userId) {
        User user = findActiveUser(userId);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = findActiveUser(userId);
        user.updateProfile(request.nickname(), request.profileImage());
        return UserResponse.from(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = findActiveUser(userId);
        user.softDelete();
    }

    private User findActiveUser(Long userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
