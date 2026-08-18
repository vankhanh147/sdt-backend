package com.sdt.feedback.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FeedbackAttachmentResponse(
        UUID id,
        UUID feedbackId,
        String originalFilename,
        String contentType,
        Long fileSize,
        OffsetDateTime createdAt,
        String downloadUrl
) {
}
