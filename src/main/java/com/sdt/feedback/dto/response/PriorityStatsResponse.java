package com.sdt.feedback.dto.response;

public record PriorityStatsResponse(
        long low,
        long medium,
        long high,
        long urgent
) {
}
