package com.sdt.feedback.dto.response;

import com.sdt.feedback.enums.FeedbackStatus;
import com.sdt.feedback.enums.SourceType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FeedbackCreateResponse(
        UUID id,
        String title,
        String content,
        String category,
        FeedbackStatus status,
        SourceType source,
        OffsetDateTime receivedAt,
        OffsetDateTime createdAt
) {
}
