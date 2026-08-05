package com.sdt.feedback.dto.response;

import com.sdt.feedback.enums.RawProcessingStatus;
import com.sdt.feedback.enums.SourceType;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record RawFeedbackDetailResponse(
        UUID id,
        SourceType source,
        String sourceRef,
        String rawTitle,
        String rawContent,
        String rawAuthorName,
        String rawAuthorContact,
        String rawLocation,
        String categoryHint,
        Map<String, Object> rawMetadata,
        OffsetDateTime receivedAt,
        RawProcessingStatus processingStatus,
        OffsetDateTime processedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
