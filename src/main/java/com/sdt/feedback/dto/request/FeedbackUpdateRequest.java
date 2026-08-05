package com.sdt.feedback.dto.request;

import com.sdt.feedback.enums.FeedbackStatus;
import jakarta.validation.constraints.Size;

public record FeedbackUpdateRequest(
        @Size(max = 500) String title,
        String content,
        @Size(max = 255) String authorName,
        @Size(max = 255) String authorContact,
        @Size(max = 500) String location,
        @Size(max = 100) String category,
        FeedbackStatus status
) {
}
