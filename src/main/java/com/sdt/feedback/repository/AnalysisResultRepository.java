package com.sdt.feedback.repository;

import com.sdt.feedback.entity.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, UUID> {

    List<AnalysisResult> findByFeedback_IdOrderByCreatedAtDesc(UUID feedbackId);
}
