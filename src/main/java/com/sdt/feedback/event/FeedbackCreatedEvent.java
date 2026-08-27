package com.sdt.feedback.event;

import java.util.UUID;

public record FeedbackCreatedEvent(
        UUID feedbackId,
        String authorName,
        String authorContact,
        String title
) {
}
