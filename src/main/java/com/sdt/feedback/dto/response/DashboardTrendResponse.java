package com.sdt.feedback.dto.response;

import com.sdt.feedback.enums.TrendInterval;

import java.time.LocalDate;
import java.util.List;

public record DashboardTrendResponse(
        LocalDate fromDate,
        LocalDate toDate,
        TrendInterval interval,
        List<TrendPointResponse> points
) {
}
