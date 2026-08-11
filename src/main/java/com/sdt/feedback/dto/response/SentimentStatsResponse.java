package com.sdt.feedback.dto.response;

public record SentimentStatsResponse(
        long positive,
        long neutral,
        long negative
) {
}
