package com.dreamlog.prompt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {

    @Query(value = "SELECT * FROM prompt_templates WHERE is_active = true ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<PromptTemplate> findRandom();

    @Query(value = "SELECT * FROM prompt_templates WHERE is_active = true AND category = :category ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<PromptTemplate> findRandomByCategory(@Param("category") String category);

    List<PromptTemplate> findByCategoryAndIsActiveTrue(PromptCategory category);
}
