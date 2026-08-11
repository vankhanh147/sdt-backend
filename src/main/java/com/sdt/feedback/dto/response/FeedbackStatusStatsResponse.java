package com.sdt.feedback.dto.response;

public record FeedbackStatusStatsResponse(
        long pendingAnalysis,
        long analyzed,
        long inProgress,
        long resolved,
        long rejected,
        long analysisFailed
) {
}
