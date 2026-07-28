package com.sdt.feedback.entity;

import com.sdt.feedback.enums.RawProcessingStatus;
import com.sdt.feedback.enums.SourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "raw_feedback",
        schema = "public",
        uniqueConstraints = @UniqueConstraint(columnNames = {"source", "source_ref"})
)
@Getter
@Setter
@NoArgsConstructor
public class RawFeedback {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 30)
    private SourceType source;

    @Column(name = "source_ref", nullable = false, length = 255)
    private String sourceRef;

    @Column(name = "raw_title", length = 500)
    private String rawTitle;

    @Column(name = "raw_content", nullable = false, columnDefinition = "text")
    private String rawContent;

    @Column(name = "raw_author_name", length = 255)
    private String rawAuthorName;

    @Column(name = "raw_author_contact", length = 255)
    private String rawAuthorContact;

    @Column(name = "raw_location", length = 500)
    private String rawLocation;

    @Column(name = "category_hint", length = 100)
    private String categoryHint;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_metadata", columnDefinition = "jsonb")
    private Map<String, Object> rawMetadata;

    @Column(name = "received_at", nullable = false)
    private OffsetDateTime receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 30)
    private RawProcessingStatus processingStatus = RawProcessingStatus.NEW;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
