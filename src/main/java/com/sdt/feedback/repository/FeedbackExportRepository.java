package com.sdt.feedback.repository;

import com.sdt.feedback.entity.Feedback;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface FeedbackExportRepository {

    List<Feedback> findExportChunk(
            Specification<Feedback> specification,
            int offset,
            int limit
    );
}
