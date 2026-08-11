package com.sdt.feedback.repository;

import com.sdt.feedback.entity.Feedback;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class FeedbackExportRepositoryImpl implements FeedbackExportRepository {

    private final EntityManager entityManager;

    public FeedbackExportRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Feedback> findExportChunk(
            Specification<Feedback> specification,
            int offset,
            int limit
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Feedback> criteriaQuery = criteriaBuilder
                .createQuery(Feedback.class);
        Root<Feedback> feedback = criteriaQuery.from(Feedback.class);
        feedback.fetch("rawFeedback", JoinType.INNER);

        Predicate predicate = specification.toPredicate(
                feedback,
                criteriaQuery,
                criteriaBuilder
        );
        criteriaQuery.select(feedback)
                .distinct(true)
                .where(predicate == null ? criteriaBuilder.conjunction() : predicate)
                .orderBy(
                        criteriaBuilder.desc(feedback.get("createdAt")),
                        criteriaBuilder.desc(feedback.get("id"))
                );

        TypedQuery<Feedback> query = entityManager.createQuery(criteriaQuery);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }
}
