package com.sdt.feedback.service;

import com.sdt.feedback.dto.response.DashboardStatsResponse;
import com.sdt.feedback.dto.response.DashboardTrendResponse;
import com.sdt.feedback.dto.response.FeedbackStatusStatsResponse;
import com.sdt.feedback.dto.response.PriorityStatsResponse;
import com.sdt.feedback.dto.response.SentimentStatsResponse;
import com.sdt.feedback.dto.response.TrendPointResponse;
import com.sdt.feedback.enums.FeedbackStatus;
import com.sdt.feedback.enums.PriorityLevel;
import com.sdt.feedback.enums.SentimentType;
import com.sdt.feedback.enums.TrendInterval;
import com.sdt.feedback.exception.InvalidFilterException;
import com.sdt.feedback.repository.AnalysisResultRepository;
import com.sdt.feedback.repository.FeedbackRepository;
import com.sdt.feedback.repository.projection.LatestAnalysisCountProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Bangkok");
    private static final int DEFAULT_DAY_RANGE = 30;
    private static final int MAX_DAY_POINTS = 366;
    private static final int MAX_MONTH_POINTS = 120;

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

    @Transactional(readOnly = true)
    public DashboardTrendResponse getTrend(
            LocalDate requestedFromDate,
            LocalDate requestedToDate,
            TrendInterval requestedInterval
    ) {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        LocalDate toDate = requestedToDate == null ? today : requestedToDate;
        LocalDate fromDate = requestedFromDate == null
                ? toDate.minusDays(DEFAULT_DAY_RANGE - 1L)
                : requestedFromDate;
        TrendInterval interval = requestedInterval == null
                ? TrendInterval.DAY
                : requestedInterval;

        validateRange(fromDate, toDate, interval);

        OffsetDateTime startInclusive = fromDate
                .atStartOfDay(BUSINESS_ZONE)
                .toOffsetDateTime();
        OffsetDateTime endExclusive = toDate
                .plusDays(1)
                .atStartOfDay(BUSINESS_ZONE)
                .toOffsetDateTime();

        Map<LocalDate, Long> counts = feedbackRepository.countTrend(
                        startInclusive,
                        endExclusive,
                        interval.name().toLowerCase(),
                        BUSINESS_ZONE.getId()
                )
                .stream()
                .collect(Collectors.toMap(
                        row -> row.getPeriod(),
                        row -> row.getCount()
                ));

        return new DashboardTrendResponse(
                fromDate,
                toDate,
                interval,
                fillMissingPeriods(fromDate, toDate, interval, counts)
        );
    }

    private void validateRange(
            LocalDate fromDate,
            LocalDate toDate,
            TrendInterval interval
    ) {
        if (fromDate.isAfter(toDate)) {
            throw new InvalidFilterException(
                    "fromDate must be before or equal to toDate"
            );
        }

        long pointCount = interval == TrendInterval.DAY
                ? ChronoUnit.DAYS.between(fromDate, toDate) + 1
                : ChronoUnit.MONTHS.between(
                        YearMonth.from(fromDate),
                        YearMonth.from(toDate)
                ) + 1;
        long maximum = interval == TrendInterval.DAY
                ? MAX_DAY_POINTS
                : MAX_MONTH_POINTS;
        if (pointCount > maximum) {
            throw new InvalidFilterException(
                    "Date range produces more than " + maximum
                            + " " + interval.name() + " points"
            );
        }
    }

    private List<TrendPointResponse> fillMissingPeriods(
            LocalDate fromDate,
            LocalDate toDate,
            TrendInterval interval,
            Map<LocalDate, Long> counts
    ) {
        List<TrendPointResponse> points = new ArrayList<>();
        if (interval == TrendInterval.DAY) {
            for (LocalDate period = fromDate;
                    !period.isAfter(toDate);
                    period = period.plusDays(1)) {
                points.add(new TrendPointResponse(
                        period,
                        counts.getOrDefault(period, 0L)
                ));
            }
            return points;
        }

        YearMonth lastMonth = YearMonth.from(toDate);
        for (YearMonth month = YearMonth.from(fromDate);
                !month.isAfter(lastMonth);
                month = month.plusMonths(1)) {
            LocalDate period = month.atDay(1);
            points.add(new TrendPointResponse(
                    period,
                    counts.getOrDefault(period, 0L)
            ));
        }
        return points;
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
