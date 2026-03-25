package com.dreamlog.diary;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    Optional<Diary> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    Page<Diary> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT d FROM Diary d WHERE d.userId = :userId AND d.deletedAt IS NULL " +
           "AND d.unlockDate <= :today ORDER BY d.unlockDate DESC")
    List<Diary> findArrivedDiaries(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Query("SELECT COUNT(d) > 0 FROM Diary d WHERE d.userId = :userId AND d.writtenDate = :date AND d.deletedAt IS NULL")
    boolean existsByUserIdAndWrittenDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
