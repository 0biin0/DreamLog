package com.dreamlog.challenge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChallengeParticipantRepository extends JpaRepository<ChallengeParticipant, Long> {

    Optional<ChallengeParticipant> findByChallengeIdAndUserId(Long challengeId, Long userId);

    boolean existsByChallengeIdAndUserId(Long challengeId, Long userId);

    int countByChallengeId(Long challengeId);

    List<ChallengeParticipant> findByUserIdAndStatus(Long userId, ParticipantStatus status);

    @Query("SELECT cp FROM ChallengeParticipant cp WHERE cp.userId = :userId")
    List<ChallengeParticipant> findByUserId(@Param("userId") Long userId);

    @Query("SELECT cp FROM ChallengeParticipant cp " +
           "JOIN Challenge c ON cp.challengeId = c.id " +
           "WHERE cp.status = 'ACTIVE' AND c.startDate <= :today AND c.endDate >= :today")
    List<ChallengeParticipant> findAllActiveInRunningChallenges(@Param("today") java.time.LocalDate today);
}
