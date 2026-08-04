package com.sdt.feedback.controller;

import com.sdt.feedback.dto.request.FeedbackIngestRequest;
import com.sdt.feedback.dto.request.FeedbackFilterRequest;
import com.sdt.feedback.dto.response.FeedbackIngestResponse;
import com.sdt.feedback.dto.response.FeedbackListItemResponse;
import com.sdt.feedback.dto.response.PageResponse;
import com.sdt.feedback.service.FeedbackIngestService;
import com.sdt.feedback.service.FeedbackQueryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackIngestService feedbackIngestService;
    private final FeedbackQueryService feedbackQueryService;

    public FeedbackController(
            FeedbackIngestService feedbackIngestService,
            FeedbackQueryService feedbackQueryService
    ) {
        this.feedbackIngestService = feedbackIngestService;
        this.feedbackQueryService = feedbackQueryService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<FeedbackListItemResponse>> getFeedbacks(
            @Valid @ModelAttribute FeedbackFilterRequest filter
    ) {
        return ResponseEntity.ok(feedbackQueryService.getFeedbacks(filter));
    }

    @PostMapping("/ingest")
    public ResponseEntity<FeedbackIngestResponse> ingest(
            @Valid @RequestBody FeedbackIngestRequest request
    ) {
        FeedbackIngestResponse response = feedbackIngestService.ingest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
