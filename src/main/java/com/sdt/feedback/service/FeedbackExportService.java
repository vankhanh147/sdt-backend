package com.sdt.feedback.service;

import com.sdt.feedback.dto.request.FeedbackFilterRequest;
import com.sdt.feedback.dto.response.FeedbackExportRow;
import com.sdt.feedback.entity.AnalysisResult;
import com.sdt.feedback.entity.Feedback;
import com.sdt.feedback.entity.RawFeedback;
import com.sdt.feedback.exception.ExportLimitExceededException;
import com.sdt.feedback.exception.InvalidFilterException;
import com.sdt.feedback.repository.AnalysisResultRepository;
import com.sdt.feedback.repository.FeedbackRepository;
import com.sdt.feedback.repository.specification.FeedbackSpecification;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FeedbackExportService {

    private static final int CHUNK_SIZE = 500;
    private static final long MAX_EXPORT_ROWS = 50_000;
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Bangkok");
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter
            .ofPattern("yyyyMMdd-HHmmss");
    private static final String[] HEADERS = {
            "id", "title", "content", "authorName", "authorContact",
            "location", "category", "status", "source", "receivedAt",
            "sentiment", "sentimentScore", "priority", "priorityScore",
            "createdAt", "updatedAt", "resolvedAt"
    };

    private final FeedbackRepository feedbackRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final EntityManager entityManager;

    public FeedbackExportService(
            FeedbackRepository feedbackRepository,
            AnalysisResultRepository analysisResultRepository,
            EntityManager entityManager
    ) {
        this.feedbackRepository = feedbackRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public ExportPlan prepareExport(FeedbackFilterRequest filter) {
        validateDateRange(filter);
        long rowCount = feedbackRepository.count(
                FeedbackSpecification.withFilters(filter)
        );
        if (rowCount > MAX_EXPORT_ROWS) {
            throw new ExportLimitExceededException(
                    "Export exceeds the maximum of " + MAX_EXPORT_ROWS + " rows"
            );
        }
        String filename = "feedback-export-"
                + OffsetDateTime.now(BUSINESS_ZONE).format(FILE_TIMESTAMP)
                + ".csv";
        return new ExportPlan(filename, rowCount);
    }

    @Transactional(readOnly = true)
    public void writeCsv(FeedbackFilterRequest filter, OutputStream outputStream)
            throws IOException {
        Specification<Feedback> specification = FeedbackSpecification
                .withFilters(filter);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                outputStream,
                StandardCharsets.UTF_8
        ));
        writer.write('\uFEFF');
        writeCells(writer, List.of(HEADERS), false);

        int offset = 0;
        while (true) {
            List<Feedback> feedbacks = feedbackRepository.findExportChunk(
                    specification,
                    offset,
                    CHUNK_SIZE
            );
            if (feedbacks.isEmpty()) {
                break;
            }

            Map<UUID, AnalysisResult> latestAnalysis = findLatestAnalysis(feedbacks);
            for (Feedback feedback : feedbacks) {
                writeRow(writer, toExportRow(
                        feedback,
                        latestAnalysis.get(feedback.getId())
                ));
            }
            writer.flush();
            offset += feedbacks.size();
            entityManager.clear();

            if (feedbacks.size() < CHUNK_SIZE) {
                break;
            }
        }
        writer.flush();
    }

    private void validateDateRange(FeedbackFilterRequest filter) {
        if (filter.getFromDate() != null
                && filter.getToDate() != null
                && filter.getFromDate().isAfter(filter.getToDate())) {
            throw new InvalidFilterException(
                    "fromDate must be before or equal to toDate"
            );
        }
    }

    private Map<UUID, AnalysisResult> findLatestAnalysis(
            Collection<Feedback> feedbacks
    ) {
        List<UUID> feedbackIds = feedbacks.stream()
                .map(Feedback::getId)
                .toList();
        if (feedbackIds.isEmpty()) {
            return Map.of();
        }
        return analysisResultRepository.findLatestForExport(feedbackIds)
                .stream()
                .collect(Collectors.toMap(
                        analysis -> analysis.getFeedback().getId(),
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
    }

    private FeedbackExportRow toExportRow(
            Feedback feedback,
            AnalysisResult analysis
    ) {
        RawFeedback rawFeedback = feedback.getRawFeedback();
        return new FeedbackExportRow(
                feedback.getId(),
                feedback.getTitle(),
                feedback.getContent(),
                feedback.getAuthorName(),
                feedback.getAuthorContact(),
                feedback.getLocation(),
                feedback.getCategory(),
                feedback.getStatus(),
                rawFeedback.getSource(),
                rawFeedback.getReceivedAt(),
                analysis == null ? null : analysis.getSentiment(),
                analysis == null ? null : analysis.getSentimentScore(),
                analysis == null ? null : analysis.getPriority(),
                analysis == null ? null : analysis.getPriorityScore(),
                feedback.getCreatedAt(),
                feedback.getUpdatedAt(),
                feedback.getResolvedAt()
        );
    }

    private void writeRow(BufferedWriter writer, FeedbackExportRow row)
            throws IOException {
        writeCells(writer, Arrays.asList(
                value(row.id()), row.title(), row.content(), row.authorName(),
                row.authorContact(), row.location(), row.category(),
                value(row.status()), value(row.source()), value(row.receivedAt()),
                value(row.sentiment()), value(row.sentimentScore()),
                value(row.priority()), value(row.priorityScore()),
                value(row.createdAt()), value(row.updatedAt()),
                value(row.resolvedAt())
        ), true);
    }

    private void writeCells(
            BufferedWriter writer,
            List<String> cells,
            boolean protectFormula
    ) throws IOException {
        for (int index = 0; index < cells.size(); index++) {
            if (index > 0) {
                writer.write(',');
            }
            writer.write(escapeCsv(cells.get(index), protectFormula));
        }
        writer.newLine();
    }

    private String escapeCsv(String value, boolean protectFormula) {
        String safeValue = value == null ? "" : value;
        if (protectFormula
                && !safeValue.isEmpty()
                && "=+-@".indexOf(safeValue.charAt(0)) >= 0) {
            safeValue = "'" + safeValue;
        }
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }

    private String value(Object value) {
        return value == null ? null : value.toString();
    }

    public record ExportPlan(String filename, long rowCount) {
    }
}
