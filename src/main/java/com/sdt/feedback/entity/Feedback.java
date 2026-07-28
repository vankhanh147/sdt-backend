package com.sdt.feedback.entity;

import com.sdt.feedback.enums.FeedbackStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "feedback", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class Feedback {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raw_feedback_id", nullable = false, unique = true)
    private RawFeedback rawFeedback;

    @Column(name = "title", length = 500)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "author_name", length = 255)
    private String authorName;

    @Column(name = "author_contact", length = 255)
    private String authorContact;

    @Column(name = "location", length = 500)
    private String location;

    @Column(name = "category", length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private FeedbackStatus status = FeedbackStatus.PENDING_ANALYSIS;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
