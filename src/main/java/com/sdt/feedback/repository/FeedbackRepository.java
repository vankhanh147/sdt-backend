package com.sdt.feedback.repository;

import com.sdt.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    Optional<Feedback> findByRawFeedback_Id(UUID rawFeedbackId);
}
