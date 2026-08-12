package com.sdt.feedback.repository;

import com.sdt.feedback.entity.AnalysisResult;
import com.sdt.feedback.repository.projection.LatestAnalysisCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, UUID> {

    List<AnalysisResult> findByFeedback_IdOrderByCreatedAtDescIdDesc(
            UUID feedbackId
    );

    @Query(value = """
            select ranked.*
            from (
                select
                    analysis.*,
                    row_number() over (
                        partition by analysis.feedback_id
                        order by analysis.created_at desc, analysis.id desc
                    ) as latest_row_number
                from public.analysis_result analysis
                where analysis.feedback_id in (:feedbackIds)
            ) ranked
            where ranked.latest_row_number = 1
            """, nativeQuery = true)
    List<AnalysisResult> findLatestByFeedbackIds(
            @Param("feedbackIds") Collection<UUID> feedbackIds
    );

    @Query(value = """
            select
                ranked.sentiment as sentiment,
                ranked.priority as priority,
                count(*) as count
            from (
                select
                    analysis.sentiment,
                    analysis.priority,
                    row_number() over (
                        partition by analysis.feedback_id
                        order by analysis.created_at desc, analysis.id desc
                    ) as row_number
                from public.analysis_result analysis
            ) ranked
            where ranked.row_number = 1
            group by ranked.sentiment, ranked.priority
            """, nativeQuery = true)
    List<LatestAnalysisCountProjection> countLatestGroupedBySentimentAndPriority();

}
