package com.dreamlog.challenge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    @Query("SELECT c FROM Challenge c WHERE c.startDate >= :today ORDER BY c.startDate ASC")
    List<Challenge> findJoinableChallenges(@Param("today") LocalDate today);

    @Query("SELECT c FROM Challenge c WHERE c.startDate <= :today AND c.endDate >= :today")
    List<Challenge> findActiveChallenges(@Param("today") LocalDate today);
}
