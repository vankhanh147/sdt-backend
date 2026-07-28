package com.sdt.feedback.dto.response;

import com.sdt.feedback.enums.RawProcessingStatus;
import com.sdt.feedback.enums.SourceType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FeedbackIngestResponse(
        UUID id,
        SourceType source,
        String sourceRef,
        RawProcessingStatus processingStatus,
        OffsetDateTime receivedAt,
        OffsetDateTime createdAt
) {
}
