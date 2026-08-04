package com.sdt.feedback.repository;

import com.sdt.feedback.entity.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, UUID> {

    List<AnalysisResult> findByFeedback_IdOrderByCreatedAtDesc(UUID feedbackId);

    @Query("""
            select analysis
            from AnalysisResult analysis
            where analysis.feedback.id in :feedbackIds
              and analysis.createdAt = (
                  select max(candidate.createdAt)
                  from AnalysisResult candidate
                  where candidate.feedback.id = analysis.feedback.id
              )
            order by analysis.createdAt desc
            """)
    List<AnalysisResult> findLatestByFeedbackIds(
            @Param("feedbackIds") Collection<UUID> feedbackIds
    );
}
