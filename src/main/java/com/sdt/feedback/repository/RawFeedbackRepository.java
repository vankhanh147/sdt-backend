package com.sdt.feedback.repository;

import com.sdt.feedback.entity.RawFeedback;
import com.sdt.feedback.enums.SourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RawFeedbackRepository extends JpaRepository<RawFeedback, UUID> {

    Optional<RawFeedback> findBySourceAndSourceRef(SourceType source, String sourceRef);

    boolean existsBySourceAndSourceRef(SourceType source, String sourceRef);
}
