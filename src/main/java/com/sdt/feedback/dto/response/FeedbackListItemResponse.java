package com.sdt.feedback.dto.response;

import com.sdt.feedback.enums.FeedbackStatus;
import com.sdt.feedback.enums.PriorityLevel;
import com.sdt.feedback.enums.SentimentType;
import com.sdt.feedback.enums.SourceType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record FeedbackListItemResponse(
        UUID id,
        String title,
        String content,
        String authorName,
        String location,
        String category,
        FeedbackStatus status,
        SourceType source,
        OffsetDateTime receivedAt,
        SentimentType sentiment,
        BigDecimal sentimentScore,
        PriorityLevel priority,
        Integer priorityScore,
        OffsetDateTime createdAt
) {
}
