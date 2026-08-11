package com.sdt.feedback.repository;

import com.sdt.feedback.entity.Feedback;
import com.sdt.feedback.repository.projection.FeedbackStatusCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

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
}
