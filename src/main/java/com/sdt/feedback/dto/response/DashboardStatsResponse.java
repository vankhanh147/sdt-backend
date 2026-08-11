package com.sdt.feedback.dto.response;

public record DashboardStatsResponse(
        long totalFeedback,
        FeedbackStatusStatsResponse status,
        SentimentStatsResponse sentiment,
        PriorityStatsResponse priority
) {
}
