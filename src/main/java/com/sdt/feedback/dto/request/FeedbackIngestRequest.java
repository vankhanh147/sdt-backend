package com.sdt.feedback.dto.request;

import com.sdt.feedback.enums.SourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.Map;

public record FeedbackIngestRequest(
        @NotNull
        SourceType source,

        @NotBlank
        @Size(max = 255)
        String sourceRef,

        @Size(max = 500)
        String rawTitle,

        @NotBlank
        String rawContent,

        @Size(max = 255)
        String rawAuthorName,

        @Size(max = 255)
        String rawAuthorContact,

        @Size(max = 500)
        String rawLocation,

        @Size(max = 100)
        String categoryHint,

        Map<String, Object> rawMetadata,

        @NotNull
        OffsetDateTime receivedAt
) {
}
