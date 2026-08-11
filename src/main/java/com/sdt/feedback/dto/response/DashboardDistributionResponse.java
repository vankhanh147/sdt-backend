package com.sdt.feedback.dto.response;

import java.util.List;

public record DashboardDistributionResponse(
        List<DistributionItemResponse> sentiment,
        List<DistributionItemResponse> priority,
        List<DistributionItemResponse> category,
        List<DistributionItemResponse> source
) {
}
