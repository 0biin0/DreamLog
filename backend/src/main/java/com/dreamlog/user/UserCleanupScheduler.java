package com.dreamlog.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 탈퇴 후 30일이 지난 사용자의 데이터를 완전 삭제하는 스케줄러.
 * 매일 KST 03:00에 실행.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void cleanupDeletedUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        List<User> expiredUsers = userRepository.findByDeletedAtBefore(threshold);

        if (expiredUsers.isEmpty()) {
            return;
        }

        log.info("탈퇴 30일 경과 사용자 {} 명 완전 삭제 시작", expiredUsers.size());

        for (User user : expiredUsers) {
            userRepository.delete(user);
            log.info("사용자 완전 삭제: id={}, email={}", user.getId(), user.getEmail());
        }

        log.info("완전 삭제 완료");
    }
}
