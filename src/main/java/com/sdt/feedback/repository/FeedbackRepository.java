package com.sdt.feedback.repository;

import com.sdt.feedback.entity.Feedback;
import com.sdt.feedback.repository.projection.FeedbackStatusCountProjection;
import com.sdt.feedback.repository.projection.CategoryCountProjection;
import com.sdt.feedback.repository.projection.SourceCountProjection;
import com.sdt.feedback.repository.projection.TrendCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedbackRepository extends
        JpaRepository<Feedback, UUID>,
        JpaSpecificationExecutor<Feedback> {

    @Override
    @EntityGraph(attributePaths = "rawFeedback")
    Page<Feedback> findAll(Specification<Feedback> specification, Pageable pageable);

    Optional<Feedback> findByRawFeedback_Id(UUID rawFeedbackId);

    @EntityGraph(attributePaths = "rawFeedback")
    Optional<Feedback> findDetailById(UUID id);

    @Query("""
            select feedback.status as status, count(feedback) as count
            from Feedback feedback
            group by feedback.status
            """)
    List<FeedbackStatusCountProjection> countGroupedByStatus();

    @Query(value = """
            select
                date_trunc(
                    :interval,
                    timezone(:timeZone, feedback.created_at)
                )::date as period,
                count(*) as count
            from public.feedback feedback
            where feedback.created_at >= :startInclusive
              and feedback.created_at < :endExclusive
            group by period
            order by period
            """, nativeQuery = true)
    List<TrendCountProjection> countTrend(
            @Param("startInclusive") OffsetDateTime startInclusive,
            @Param("endExclusive") OffsetDateTime endExclusive,
            @Param("interval") String interval,
            @Param("timeZone") String timeZone
    );

    @Query("""
            select
                feedback.category as category,
                count(feedback) as count
            from Feedback feedback
            where feedback.category is not null
              and trim(feedback.category) <> ''
            group by feedback.category
            order by count(feedback) desc, feedback.category asc
            """)
    List<CategoryCountProjection> countGroupedByCategory();

    @Query("""
            select
                rawFeedback.source as source,
                count(feedback) as count
            from Feedback feedback
            join feedback.rawFeedback rawFeedback
            group by rawFeedback.source
            """)
    List<SourceCountProjection> countGroupedBySource();
}
