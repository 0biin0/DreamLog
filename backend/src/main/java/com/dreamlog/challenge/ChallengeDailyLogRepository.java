package com.dreamlog.challenge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ChallengeDailyLogRepository extends JpaRepository<ChallengeDailyLog, Long> {

    Optional<ChallengeDailyLog> findByParticipantIdAndLogDate(Long participantId, LocalDate logDate);

    boolean existsByParticipantIdAndLogDate(Long participantId, LocalDate logDate);

    List<ChallengeDailyLog> findByParticipantIdOrderByLogDateAsc(Long participantId);

    @Query("SELECT MAX(cdl.logDate) FROM ChallengeDailyLog cdl WHERE cdl.participantId = :participantId")
    Optional<LocalDate> findLastCheckedDate(@Param("participantId") Long participantId);
}
