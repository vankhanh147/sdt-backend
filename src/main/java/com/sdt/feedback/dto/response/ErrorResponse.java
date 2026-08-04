package com.sdt.feedback.dto.response;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(
        OffsetDateTime timestamp,
        int status, //Lưu mã HTTP dạng số.
        String error, //Lưu tên ngắn của HTTP status.
        String message,
        String path,
        Map<String, String> validationErrors
) {
}
