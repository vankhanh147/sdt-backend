package com.sdt.feedback.controller;

import com.sdt.feedback.dto.request.FeedbackFilterRequest;
import com.sdt.feedback.service.FeedbackExportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private static final MediaType CSV_MEDIA_TYPE = MediaType.parseMediaType(
            "text/csv;charset=UTF-8"
    );

    private final FeedbackExportService feedbackExportService;

    public ExportController(FeedbackExportService feedbackExportService) {
        this.feedbackExportService = feedbackExportService;
    }

    @GetMapping
    public ResponseEntity<StreamingResponseBody> exportFeedback(
            @Valid @ModelAttribute FeedbackFilterRequest filter
    ) {
        FeedbackExportService.ExportPlan plan = feedbackExportService
                .prepareExport(filter);
        StreamingResponseBody body = outputStream -> feedbackExportService
                .writeCsv(filter, outputStream);

        return ResponseEntity.ok()
                .contentType(CSV_MEDIA_TYPE)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + plan.filename() + "\""
                )
                .header("X-Content-Type-Options", "nosniff")
                .body(body);
    }
}
