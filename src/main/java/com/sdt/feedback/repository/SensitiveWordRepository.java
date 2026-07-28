package com.sdt.feedback.repository;

import com.sdt.feedback.entity.SensitiveWord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, UUID> {

    Optional<SensitiveWord> findByKeyword(String keyword);

    boolean existsByKeyword(String keyword);

    List<SensitiveWord> findByIsActiveTrue();
}
