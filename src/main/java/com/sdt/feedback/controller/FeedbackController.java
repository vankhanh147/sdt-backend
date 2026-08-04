package com.sdt.feedback.controller;

import com.sdt.feedback.dto.request.FeedbackIngestRequest;
import com.sdt.feedback.dto.response.FeedbackIngestResponse;
import com.sdt.feedback.service.FeedbackIngestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackIngestService feedbackIngestService;

    public FeedbackController(FeedbackIngestService feedbackIngestService) {
        this.feedbackIngestService = feedbackIngestService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<FeedbackIngestResponse> ingest(
            @Valid @RequestBody FeedbackIngestRequest request
    ) {
        FeedbackIngestResponse response = feedbackIngestService.ingest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
