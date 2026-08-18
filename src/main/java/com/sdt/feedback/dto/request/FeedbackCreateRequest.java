package com.sdt.feedback.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record FeedbackCreateRequest(
        @Size(max = 500) String title,
        @NotBlank String content,
        @Size(max = 255) String authorName,
        @Size(max = 255) String authorContact,
        @Size(max = 500) String location,
        @Size(max = 100) String category,
        OffsetDateTime receivedAt
) {
}
