package com.sdt.feedback.mapper;

import com.sdt.feedback.dto.request.FeedbackIngestRequest;
import com.sdt.feedback.dto.response.FeedbackIngestResponse;
import com.sdt.feedback.entity.RawFeedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface RawFeedbackMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "processingStatus", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    RawFeedback toEntity(FeedbackIngestRequest request);

    FeedbackIngestResponse toResponse(RawFeedback rawFeedback);
}
