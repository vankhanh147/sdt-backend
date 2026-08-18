package com.sdt.feedback.service;

import com.sdt.feedback.client.SupabaseStorageClient;
import com.sdt.feedback.dto.request.FeedbackUpdateRequest;
import com.sdt.feedback.dto.response.AnalysisResultResponse;
import com.sdt.feedback.dto.response.FeedbackDetailResponse;
import com.sdt.feedback.dto.response.RawFeedbackDetailResponse;
import com.sdt.feedback.entity.AnalysisResult;
import com.sdt.feedback.entity.Feedback;
import com.sdt.feedback.enums.FeedbackStatus;
import com.sdt.feedback.exception.InvalidUpdateException;
import com.sdt.feedback.exception.ResourceNotFoundException;
import com.sdt.feedback.mapper.FeedbackMapper;
import com.sdt.feedback.repository.AnalysisResultRepository;
import com.sdt.feedback.repository.FeedbackRepository;
import com.sdt.feedback.repository.FeedbackAttachmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class FeedbackCommandService {

    private final FeedbackRepository feedbackRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final FeedbackMapper feedbackMapper;
    private final FeedbackAttachmentRepository feedbackAttachmentRepository;
    private final SupabaseStorageClient storageClient;

    public FeedbackCommandService(
            FeedbackRepository feedbackRepository,
            AnalysisResultRepository analysisResultRepository,
            FeedbackMapper feedbackMapper,
            FeedbackAttachmentRepository feedbackAttachmentRepository,
            SupabaseStorageClient storageClient
    ) {
        this.feedbackRepository = feedbackRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.feedbackMapper = feedbackMapper;
        this.feedbackAttachmentRepository = feedbackAttachmentRepository;
        this.storageClient = storageClient;
    }

    @Transactional
    public FeedbackDetailResponse updateFeedback(
            UUID id,
            FeedbackUpdateRequest request
    ) {
        FeedbackUpdateRequest normalizedRequest = normalize(request);
        Feedback feedback = feedbackRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Feedback not found with id=" + id
                ));
        FeedbackStatus previousStatus = feedback.getStatus();

        feedbackMapper.updateEntity(normalizedRequest, feedback);
        updateResolvedAt(feedback, previousStatus, normalizedRequest.status());
        Feedback savedFeedback = feedbackRepository.saveAndFlush(feedback);

        List<AnalysisResult> analysisResults = analysisResultRepository
                .findByFeedback_IdOrderByCreatedAtDesc(id);
        List<AnalysisResultResponse> analysisHistory = feedbackMapper
                .toAnalysisResultResponses(analysisResults);
        AnalysisResultResponse latestAnalysis = analysisHistory.isEmpty()
                ? null
                : analysisHistory.getFirst();
        RawFeedbackDetailResponse rawFeedback = feedbackMapper
                .toRawFeedbackDetailResponse(savedFeedback.getRawFeedback());

        return feedbackMapper.toDetailResponse(
                savedFeedback,
                rawFeedback,
                latestAnalysis,
                analysisHistory
        );
    }

    @Transactional
    public void deleteFeedback(UUID id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Feedback not found with id=" + id
                ));

        List<String> storagePaths = feedbackAttachmentRepository
                .findStoragePathsByFeedbackId(id);
        storageClient.deleteAll(storagePaths);

        feedbackRepository.delete(feedback);
        feedbackRepository.flush();
    }

    private FeedbackUpdateRequest normalize(FeedbackUpdateRequest request) {
        if (request.title() == null
                && request.content() == null
                && request.authorName() == null
                && request.authorContact() == null
                && request.location() == null
                && request.category() == null
                && request.status() == null) {
            throw new InvalidUpdateException(
                    "At least one field must be provided for update"
            );
        }

        return new FeedbackUpdateRequest(
                trimRequiredWhenProvided(request.title(), "title"),
                trimRequiredWhenProvided(request.content(), "content"),
                trimRequiredWhenProvided(request.authorName(), "authorName"),
                trimRequiredWhenProvided(request.authorContact(), "authorContact"),
                trimRequiredWhenProvided(request.location(), "location"),
                trimRequiredWhenProvided(request.category(), "category"),
                request.status()
        );
    }

    private String trimRequiredWhenProvided(String value, String fieldName) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            throw new InvalidUpdateException(
                    fieldName + " must not be blank when provided"
            );
        }
        return trimmedValue;
    }

    private void updateResolvedAt(
            Feedback feedback,
            FeedbackStatus previousStatus,
            FeedbackStatus requestedStatus
    ) {
        if (requestedStatus == null) {
            return;
        }

        if (requestedStatus == FeedbackStatus.RESOLVED) {
            if (previousStatus != FeedbackStatus.RESOLVED
                    || feedback.getResolvedAt() == null) {
                feedback.setResolvedAt(
                        OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS)
                );
            }
        } else if (previousStatus == FeedbackStatus.RESOLVED) {
            feedback.setResolvedAt(null);
        }
    }
}
