package com.sdt.feedback.service;

import com.sdt.feedback.dto.response.DashboardStatsResponse;
import com.sdt.feedback.dto.response.FeedbackStatusStatsResponse;
import com.sdt.feedback.dto.response.PriorityStatsResponse;
import com.sdt.feedback.dto.response.SentimentStatsResponse;
import com.sdt.feedback.enums.FeedbackStatus;
import com.sdt.feedback.enums.PriorityLevel;
import com.sdt.feedback.enums.SentimentType;
import com.sdt.feedback.repository.AnalysisResultRepository;
import com.sdt.feedback.repository.FeedbackRepository;
import com.sdt.feedback.repository.projection.LatestAnalysisCountProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;

@Service
public class DashboardService {

    private final FeedbackRepository feedbackRepository;
    private final AnalysisResultRepository analysisResultRepository;

    public DashboardService(
            FeedbackRepository feedbackRepository,
            AnalysisResultRepository analysisResultRepository
    ) {
        this.feedbackRepository = feedbackRepository;
        this.analysisResultRepository = analysisResultRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        long totalFeedback = feedbackRepository.count();

        EnumMap<FeedbackStatus, Long> statusCounts = zeroCounts(
                FeedbackStatus.class
        );
        feedbackRepository.countGroupedByStatus()
                .forEach(row -> statusCounts.put(row.getStatus(), row.getCount()));

        EnumMap<SentimentType, Long> sentimentCounts = zeroCounts(
                SentimentType.class
        );
        EnumMap<PriorityLevel, Long> priorityCounts = zeroCounts(
                PriorityLevel.class
        );
        for (LatestAnalysisCountProjection row
                : analysisResultRepository.countLatestGroupedBySentimentAndPriority()) {
            if (row.getSentiment() != null) {
                sentimentCounts.merge(
                        SentimentType.valueOf(row.getSentiment()),
                        row.getCount(),
                        Long::sum
                );
            }
            if (row.getPriority() != null) {
                priorityCounts.merge(
                        PriorityLevel.valueOf(row.getPriority()),
                        row.getCount(),
                        Long::sum
                );
            }
        }

        return new DashboardStatsResponse(
                totalFeedback,
                toStatusResponse(statusCounts),
                toSentimentResponse(sentimentCounts),
                toPriorityResponse(priorityCounts)
        );
    }

    private <E extends Enum<E>> EnumMap<E, Long> zeroCounts(Class<E> enumType) {
        EnumMap<E, Long> counts = new EnumMap<>(enumType);
        for (E value : enumType.getEnumConstants()) {
            counts.put(value, 0L);
        }
        return counts;
    }

    private FeedbackStatusStatsResponse toStatusResponse(
            EnumMap<FeedbackStatus, Long> counts
    ) {
        return new FeedbackStatusStatsResponse(
                counts.get(FeedbackStatus.PENDING_ANALYSIS),
                counts.get(FeedbackStatus.ANALYZED),
                counts.get(FeedbackStatus.IN_PROGRESS),
                counts.get(FeedbackStatus.RESOLVED),
                counts.get(FeedbackStatus.REJECTED),
                counts.get(FeedbackStatus.ANALYSIS_FAILED)
        );
    }

    private SentimentStatsResponse toSentimentResponse(
            EnumMap<SentimentType, Long> counts
    ) {
        return new SentimentStatsResponse(
                counts.get(SentimentType.POSITIVE),
                counts.get(SentimentType.NEUTRAL),
                counts.get(SentimentType.NEGATIVE)
        );
    }

    private PriorityStatsResponse toPriorityResponse(
            EnumMap<PriorityLevel, Long> counts
    ) {
        return new PriorityStatsResponse(
                counts.get(PriorityLevel.LOW),
                counts.get(PriorityLevel.MEDIUM),
                counts.get(PriorityLevel.HIGH),
                counts.get(PriorityLevel.URGENT)
        );
    }
}
