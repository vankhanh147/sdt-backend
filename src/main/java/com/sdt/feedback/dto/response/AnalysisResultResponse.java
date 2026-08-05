package com.sdt.feedback.dto.response;

import com.sdt.feedback.enums.AnalysisStatus;
import com.sdt.feedback.enums.PriorityLevel;
import com.sdt.feedback.enums.SentimentType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AnalysisResultResponse(
        UUID id,
        SentimentType sentiment,
        BigDecimal sentimentScore,
        String category,
        BigDecimal categoryScore,
        List<String> matchedKeywords,
        PriorityLevel priority,
        Integer priorityScore,
        String priorityReason,
        String modelName,
        String modelVersion,
        AnalysisStatus analysisStatus,
        String errorMessage,
        OffsetDateTime analyzedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
