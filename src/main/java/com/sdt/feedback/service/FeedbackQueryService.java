package com.sdt.feedback.service;

import com.sdt.feedback.dto.request.FeedbackFilterRequest;
import com.sdt.feedback.dto.response.FeedbackListItemResponse;
import com.sdt.feedback.dto.response.PageResponse;
import com.sdt.feedback.entity.AnalysisResult;
import com.sdt.feedback.entity.Feedback;
import com.sdt.feedback.exception.InvalidFilterException;
import com.sdt.feedback.mapper.FeedbackMapper;
import com.sdt.feedback.repository.AnalysisResultRepository;
import com.sdt.feedback.repository.FeedbackRepository;
import com.sdt.feedback.repository.specification.FeedbackSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FeedbackQueryService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_SORT_BY = "createdAt";
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt",
            "updatedAt",
            "title",
            "status",
            "category"
    );

    private final FeedbackRepository feedbackRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final FeedbackMapper feedbackMapper;

    public FeedbackQueryService(
            FeedbackRepository feedbackRepository,
            AnalysisResultRepository analysisResultRepository,
            FeedbackMapper feedbackMapper
    ) {
        this.feedbackRepository = feedbackRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.feedbackMapper = feedbackMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<FeedbackListItemResponse> getFeedbacks(FeedbackFilterRequest filter) {
        validateFilter(filter);

        int pageNumber = filter.getPage() == null ? DEFAULT_PAGE : filter.getPage();
        int pageSize = filter.getSize() == null ? DEFAULT_SIZE : filter.getSize();
        String sortBy = normalizeSortBy(filter.getSortBy());
        Sort.Direction direction = normalizeSortDirection(filter.getSortDirection());
        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by(direction, sortBy)
        );

        Specification<Feedback> specification = FeedbackSpecification.withFilters(filter);
        Page<Feedback> feedbackPage = feedbackRepository.findAll(specification, pageable);
        Map<UUID, AnalysisResult> latestAnalysisByFeedbackId = findLatestAnalysis(
                feedbackPage.getContent()
        );

        List<FeedbackListItemResponse> content = feedbackPage.getContent()
                .stream()
                .map(feedback -> feedbackMapper.toListItem(
                        feedback,
                        feedback.getRawFeedback(),
                        latestAnalysisByFeedbackId.get(feedback.getId())
                ))
                .toList();

        return new PageResponse<>(
                content,
                feedbackPage.getNumber(),
                feedbackPage.getSize(),
                feedbackPage.getTotalElements(),
                feedbackPage.getTotalPages(),
                feedbackPage.isFirst(),
                feedbackPage.isLast()
        );
    }

    private void validateFilter(FeedbackFilterRequest filter) {
        if (filter.getPage() != null && filter.getPage() < 0) {
            throw new InvalidFilterException("Page must be greater than or equal to 0");
        }
        if (filter.getSize() != null
                && (filter.getSize() < 1 || filter.getSize() > MAX_SIZE)) {
            throw new InvalidFilterException("Size must be between 1 and 100");
        }
        if (filter.getFromDate() != null
                && filter.getToDate() != null
                && filter.getFromDate().isAfter(filter.getToDate())) {
            throw new InvalidFilterException("fromDate must be before or equal to toDate");
        }
    }

    private String normalizeSortBy(String requestedSortBy) {
        if (requestedSortBy == null || !ALLOWED_SORT_FIELDS.contains(requestedSortBy)) {
            return DEFAULT_SORT_BY;
        }
        return requestedSortBy;
    }

    private Sort.Direction normalizeSortDirection(String requestedDirection) {
        if (requestedDirection == null || requestedDirection.isBlank()) {
            return Sort.Direction.DESC;
        }

        return switch (requestedDirection.trim().toLowerCase(Locale.ROOT)) {
            case "asc" -> Sort.Direction.ASC;
            case "desc" -> Sort.Direction.DESC;
            default -> throw new InvalidFilterException(
                    "sortDirection must be either asc or desc"
            );
        };
    }

    private Map<UUID, AnalysisResult> findLatestAnalysis(Collection<Feedback> feedbacks) {
        List<UUID> feedbackIds = feedbacks.stream()
                .map(Feedback::getId)
                .toList();

        if (feedbackIds.isEmpty()) {
            return Map.of();
        }

        return analysisResultRepository.findLatestByFeedbackIds(feedbackIds)
                .stream()
                .collect(Collectors.toMap(
                        analysis -> analysis.getFeedback().getId(),
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
    }
}
