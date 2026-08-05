package com.sdt.feedback.controller;

import com.sdt.feedback.dto.request.FeedbackIngestRequest;
import com.sdt.feedback.dto.request.FeedbackFilterRequest;
import com.sdt.feedback.dto.request.FeedbackUpdateRequest;
import com.sdt.feedback.dto.response.FeedbackIngestResponse;
import com.sdt.feedback.dto.response.FeedbackDetailResponse;
import com.sdt.feedback.dto.response.FeedbackListItemResponse;
import com.sdt.feedback.dto.response.PageResponse;
import com.sdt.feedback.service.FeedbackCommandService;
import com.sdt.feedback.service.FeedbackIngestService;
import com.sdt.feedback.service.FeedbackQueryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackIngestService feedbackIngestService;
    private final FeedbackQueryService feedbackQueryService;
    private final FeedbackCommandService feedbackCommandService;

    public FeedbackController(
            FeedbackIngestService feedbackIngestService,
            FeedbackQueryService feedbackQueryService,
            FeedbackCommandService feedbackCommandService
    ) {
        this.feedbackIngestService = feedbackIngestService;
        this.feedbackQueryService = feedbackQueryService;
        this.feedbackCommandService = feedbackCommandService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<FeedbackListItemResponse>> getFeedbacks(
            @Valid @ModelAttribute FeedbackFilterRequest filter
    ) {
        return ResponseEntity.ok(feedbackQueryService.getFeedbacks(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedbackDetailResponse> getFeedbackDetail(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(feedbackQueryService.getFeedbackDetail(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FeedbackDetailResponse> updateFeedback(
            @PathVariable UUID id,
            @Valid @RequestBody FeedbackUpdateRequest request
    ) {
        return ResponseEntity.ok(feedbackCommandService.updateFeedback(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable UUID id) {
        feedbackCommandService.deleteFeedback(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/ingest")
    public ResponseEntity<FeedbackIngestResponse> ingest(
            @Valid @RequestBody FeedbackIngestRequest request
    ) {
        FeedbackIngestResponse response = feedbackIngestService.ingest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
