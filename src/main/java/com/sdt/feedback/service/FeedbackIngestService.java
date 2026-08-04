package com.sdt.feedback.service;

import com.sdt.feedback.dto.request.FeedbackIngestRequest;
import com.sdt.feedback.dto.response.FeedbackIngestResponse;
import com.sdt.feedback.entity.RawFeedback;
import com.sdt.feedback.enums.RawProcessingStatus;
import com.sdt.feedback.exception.DuplicateFeedbackException;
import com.sdt.feedback.mapper.RawFeedbackMapper;
import com.sdt.feedback.repository.RawFeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackIngestService {

    private final RawFeedbackRepository rawFeedbackRepository; // Service cần Repository để kiểm tra và lưu dữ liệu.
    private final RawFeedbackMapper rawFeedbackMapper;

    //Constructor:
    public FeedbackIngestService(
            RawFeedbackRepository rawFeedbackRepository,
            RawFeedbackMapper rawFeedbackMapper
    ) {
        this.rawFeedbackRepository = rawFeedbackRepository;
        this.rawFeedbackMapper = rawFeedbackMapper;
    }

    @Transactional
    public FeedbackIngestResponse ingest(FeedbackIngestRequest request) { //nhận FeedbackIngestRequest
        if (rawFeedbackRepository.existsBySourceAndSourceRef(
                request.source(),
                request.sourceRef()
        )) {
            throw new DuplicateFeedbackException( 
                    "Feedback already exists for source=%s and sourceRef=%s"
                            .formatted(request.source(), request.sourceRef())
            );
        }

        RawFeedback rawFeedback = rawFeedbackMapper.toEntity(request);
        rawFeedback.setProcessingStatus(RawProcessingStatus.NEW); //đặt trạng thái NEW

        RawFeedback savedFeedback = rawFeedbackRepository.saveAndFlush(rawFeedback); //lưu bằng RawFeedbackRepository

        return rawFeedbackMapper.toResponse(savedFeedback);
    }
}
