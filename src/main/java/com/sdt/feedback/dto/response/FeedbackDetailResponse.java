package com.sdt.feedback.dto.response;

import com.sdt.feedback.enums.FeedbackStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record FeedbackDetailResponse(
        UUID id,
        String title,
        String content,
        String authorName,
        String authorContact,
        String location,
        String category,
        FeedbackStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime resolvedAt,
        RawFeedbackDetailResponse rawFeedback,
        AnalysisResultResponse latestAnalysis,
        List<AnalysisResultResponse> analysisHistory
) {
}
