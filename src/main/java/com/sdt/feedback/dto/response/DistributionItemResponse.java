package com.sdt.feedback.dto.response;

public record DistributionItemResponse(
        String key,
        String label,
        long count
) {
}
