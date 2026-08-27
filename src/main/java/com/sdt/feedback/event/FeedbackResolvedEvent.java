package com.sdt.feedback.event;

import java.util.UUID;

public record FeedbackResolvedEvent(
        UUID feedbackId,
        String authorName,
        String authorContact,
        String title
) {
}
