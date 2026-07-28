package com.sdt.feedback.entity;

import com.sdt.feedback.enums.AnalysisStatus;
import com.sdt.feedback.enums.PriorityLevel;
import com.sdt.feedback.enums.SentimentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "analysis_result", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class AnalysisResult {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment", length = 30)
    private SentimentType sentiment;

    @Column(name = "sentiment_score", precision = 5, scale = 4)
    private BigDecimal sentimentScore;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "category_score", precision = 5, scale = 4)
    private BigDecimal categoryScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matched_keywords", columnDefinition = "jsonb")
    private List<String> matchedKeywords;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 30)
    private PriorityLevel priority;

    @Column(name = "priority_score")
    private Integer priorityScore;

    @Column(name = "priority_reason", columnDefinition = "text")
    private String priorityReason;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_status", nullable = false, length = 30)
    private AnalysisStatus analysisStatus = AnalysisStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "analyzed_at")
    private OffsetDateTime analyzedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
