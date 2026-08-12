package com.sdt.feedback.repository.specification;

import com.sdt.feedback.dto.request.FeedbackFilterRequest;
import com.sdt.feedback.entity.AnalysisResult;
import com.sdt.feedback.entity.Feedback;
import com.sdt.feedback.entity.RawFeedback;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FeedbackSpecification {

    private FeedbackSpecification() {
    }

    public static Specification<Feedback> withFilters(FeedbackFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getSource() != null) {
                Join<Feedback, RawFeedback> rawFeedback = root.join("rawFeedback", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(
                        rawFeedback.get("source"),
                        filter.getSource()
                ));
            }

            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            if (hasText(filter.getCategory())) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("category")),
                        filter.getCategory().trim().toLowerCase(Locale.ROOT)
                ));
            }

            if (hasText(filter.getKeyword())) {
                String keywordPattern = "%"
                        + escapeLike(filter.getKeyword().trim().toLowerCase(Locale.ROOT))
                        + "%";
                Predicate titleMatches = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        keywordPattern,
                        '\\'
                );
                Predicate contentMatches = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("content")),
                        keywordPattern,
                        '\\'
                );
                predicates.add(criteriaBuilder.or(titleMatches, contentMatches));
            }

            if (filter.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        filter.getFromDate()
                ));
            }

            if (filter.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"),
                        filter.getToDate()
                ));
            }

            if (filter.getSentiment() != null || filter.getPriority() != null) {
                Subquery<Integer> matchingLatestAnalysis = query.subquery(Integer.class);
                Root<AnalysisResult> analysis = matchingLatestAnalysis.from(AnalysisResult.class);
                List<Predicate> analysisPredicates = new ArrayList<>();
                analysisPredicates.add(criteriaBuilder.equal(analysis.get("feedback"), root));

                Subquery<Integer> newerAnalysisExists = matchingLatestAnalysis
                        .subquery(Integer.class);
                Root<AnalysisResult> candidate = newerAnalysisExists
                        .from(AnalysisResult.class);
                Predicate newerCreatedAt = criteriaBuilder.greaterThan(
                        candidate.get("createdAt"),
                        analysis.get("createdAt")
                );
                Predicate sameCreatedAtWithLargerId = criteriaBuilder.and(
                        criteriaBuilder.equal(
                                candidate.get("createdAt"),
                                analysis.get("createdAt")
                        ),
                        criteriaBuilder.greaterThan(
                                candidate.get("id"),
                                analysis.get("id")
                        )
                );
                newerAnalysisExists.select(criteriaBuilder.literal(1));
                newerAnalysisExists.where(
                        criteriaBuilder.equal(
                                candidate.get("feedback"),
                                analysis.get("feedback")
                        ),
                        criteriaBuilder.or(
                                newerCreatedAt,
                                sameCreatedAtWithLargerId
                        )
                );
                analysisPredicates.add(
                        criteriaBuilder.not(criteriaBuilder.exists(newerAnalysisExists))
                );

                if (filter.getSentiment() != null) {
                    analysisPredicates.add(criteriaBuilder.equal(
                            analysis.get("sentiment"),
                            filter.getSentiment()
                    ));
                }

                if (filter.getPriority() != null) {
                    analysisPredicates.add(criteriaBuilder.equal(
                            analysis.get("priority"),
                            filter.getPriority()
                    ));
                }

                matchingLatestAnalysis.select(criteriaBuilder.literal(1));
                matchingLatestAnalysis.where(analysisPredicates.toArray(Predicate[]::new));
                predicates.add(criteriaBuilder.exists(matchingLatestAnalysis));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
