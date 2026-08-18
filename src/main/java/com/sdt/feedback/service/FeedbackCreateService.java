package com.sdt.feedback.service;

import com.sdt.feedback.dto.request.FeedbackCreateRequest;
import com.sdt.feedback.dto.response.FeedbackCreateResponse;
import com.sdt.feedback.entity.Feedback;
import com.sdt.feedback.entity.RawFeedback;
import com.sdt.feedback.enums.FeedbackStatus;
import com.sdt.feedback.enums.RawProcessingStatus;
import com.sdt.feedback.enums.SourceType;
import com.sdt.feedback.mapper.FeedbackCreateMapper;
import com.sdt.feedback.repository.FeedbackRepository;
import com.sdt.feedback.repository.RawFeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class FeedbackCreateService {

    private static final String MANUAL_SOURCE_REF_PREFIX = "MANUAL-";

    private final RawFeedbackRepository rawFeedbackRepository;
    private final FeedbackRepository feedbackRepository;
    private final FeedbackCreateMapper feedbackCreateMapper;

    public FeedbackCreateService(
            RawFeedbackRepository rawFeedbackRepository,
            FeedbackRepository feedbackRepository,
            FeedbackCreateMapper feedbackCreateMapper
    ) {
        this.rawFeedbackRepository = rawFeedbackRepository;
        this.feedbackRepository = feedbackRepository;
        this.feedbackCreateMapper = feedbackCreateMapper;
    }

    @Transactional
    public FeedbackCreateResponse create(FeedbackCreateRequest request) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC)
                .truncatedTo(ChronoUnit.MICROS);
        FeedbackCreateRequest normalizedRequest = normalize(request, now);

        RawFeedback rawFeedback = feedbackCreateMapper.toRawFeedback(
                normalizedRequest
        );
        rawFeedback.setSource(SourceType.MANUAL);
        rawFeedback.setSourceRef(
                MANUAL_SOURCE_REF_PREFIX + UUID.randomUUID()
        );
        rawFeedback.setProcessingStatus(RawProcessingStatus.PROCESSED);
        rawFeedback.setProcessedAt(now);
        RawFeedback savedRawFeedback = rawFeedbackRepository.save(rawFeedback);

        Feedback feedback = feedbackCreateMapper.toFeedback(normalizedRequest);
        feedback.setRawFeedback(savedRawFeedback);
        feedback.setStatus(FeedbackStatus.PENDING_ANALYSIS);
        Feedback savedFeedback = feedbackRepository.saveAndFlush(feedback);

        return feedbackCreateMapper.toResponse(
                savedFeedback,
                savedRawFeedback
        );
    }

    private FeedbackCreateRequest normalize(
            FeedbackCreateRequest request,
            OffsetDateTime defaultReceivedAt
    ) {
        return new FeedbackCreateRequest(
                normalizeOptional(request.title()),
                request.content().trim(),
                normalizeOptional(request.authorName()),
                normalizeOptional(request.authorContact()),
                normalizeOptional(request.location()),
                normalizeOptional(request.category()),
                request.receivedAt() == null
                        ? defaultReceivedAt
                        : request.receivedAt()
        );
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
