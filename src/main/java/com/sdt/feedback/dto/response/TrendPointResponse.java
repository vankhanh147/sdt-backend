package com.sdt.feedback.dto.response;

import java.time.LocalDate;

public record TrendPointResponse(
        LocalDate period,
        long count
) {
}
