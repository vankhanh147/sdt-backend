package com.sdt.feedback.service;

import com.sdt.feedback.dto.request.FeedbackIngestRequest;
import com.sdt.feedback.dto.response.FeedbackIngestResponse;
import com.sdt.feedback.entity.RawFeedback;
import com.sdt.feedback.enums.RawProcessingStatus;
import com.sdt.feedback.exception.DuplicateFeedbackException;
import com.sdt.feedback.repository.RawFeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackIngestService {

    private final RawFeedbackRepository rawFeedbackRepository;

    public FeedbackIngestService(RawFeedbackRepository rawFeedbackRepository) {
        this.rawFeedbackRepository = rawFeedbackRepository;
    }

    @Transactional
    public FeedbackIngestResponse ingest(FeedbackIngestRequest request) {
        if (rawFeedbackRepository.existsBySourceAndSourceRef(
                request.source(),
                request.sourceRef()
        )) {
            throw new DuplicateFeedbackException(
                    "Feedback already exists for source=%s and sourceRef=%s"
                            .formatted(request.source(), request.sourceRef())
            );
        }

        RawFeedback rawFeedback = new RawFeedback();
        rawFeedback.setSource(request.source());
        rawFeedback.setSourceRef(request.sourceRef());
        rawFeedback.setRawTitle(request.rawTitle());
        rawFeedback.setRawContent(request.rawContent());
        rawFeedback.setRawAuthorName(request.rawAuthorName());
        rawFeedback.setRawAuthorContact(request.rawAuthorContact());
        rawFeedback.setRawLocation(request.rawLocation());
        rawFeedback.setCategoryHint(request.categoryHint());
        rawFeedback.setRawMetadata(request.rawMetadata());
        rawFeedback.setReceivedAt(request.receivedAt());
        rawFeedback.setProcessingStatus(RawProcessingStatus.NEW);

        RawFeedback savedFeedback = rawFeedbackRepository.saveAndFlush(rawFeedback);

        return new FeedbackIngestResponse(
                savedFeedback.getId(),
                savedFeedback.getSource(),
                savedFeedback.getSourceRef(),
                savedFeedback.getProcessingStatus(),
                savedFeedback.getReceivedAt(),
                savedFeedback.getCreatedAt()
        );
    }
}
