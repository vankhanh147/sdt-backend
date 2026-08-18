package com.sdt.feedback.dto.response;

import com.sdt.feedback.enums.NotificationType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String message,
        UUID relatedFeedbackId,
        Boolean isRead,
        OffsetDateTime readAt,
        OffsetDateTime createdAt
) {
}
