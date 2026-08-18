package com.sdt.feedback.repository;

import com.sdt.feedback.entity.FeedbackAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedbackAttachmentRepository
        extends JpaRepository<FeedbackAttachment, UUID> {

    List<FeedbackAttachment> findByFeedback_IdOrderByCreatedAtAscIdAsc(
            UUID feedbackId
    );

    Optional<FeedbackAttachment> findByIdAndFeedback_Id(
            UUID attachmentId,
            UUID feedbackId
    );

    long countByFeedback_Id(UUID feedbackId);

    @Query("""
            select attachment.storagePath
            from FeedbackAttachment attachment
            where attachment.feedback.id = :feedbackId
            """)
    List<String> findStoragePathsByFeedbackId(
            @Param("feedbackId") UUID feedbackId
    );
}
