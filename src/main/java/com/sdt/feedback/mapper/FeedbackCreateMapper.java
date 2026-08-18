package com.sdt.feedback.mapper;

import com.sdt.feedback.dto.request.FeedbackCreateRequest;
import com.sdt.feedback.dto.response.FeedbackCreateResponse;
import com.sdt.feedback.entity.Feedback;
import com.sdt.feedback.entity.RawFeedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface FeedbackCreateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "sourceRef", ignore = true)
    @Mapping(target = "rawTitle", source = "title")
    @Mapping(target = "rawContent", source = "content")
    @Mapping(target = "rawAuthorName", source = "authorName")
    @Mapping(target = "rawAuthorContact", source = "authorContact")
    @Mapping(target = "rawLocation", source = "location")
    @Mapping(target = "categoryHint", source = "category")
    @Mapping(target = "rawMetadata", ignore = true)
    @Mapping(target = "processingStatus", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    RawFeedback toRawFeedback(FeedbackCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rawFeedback", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Feedback toFeedback(FeedbackCreateRequest request);

    @Mapping(target = "id", source = "feedback.id")
    @Mapping(target = "title", source = "feedback.title")
    @Mapping(target = "content", source = "feedback.content")
    @Mapping(target = "category", source = "feedback.category")
    @Mapping(target = "status", source = "feedback.status")
    @Mapping(target = "source", source = "rawFeedback.source")
    @Mapping(target = "receivedAt", source = "rawFeedback.receivedAt")
    @Mapping(target = "createdAt", source = "feedback.createdAt")
    FeedbackCreateResponse toResponse(
            Feedback feedback,
            RawFeedback rawFeedback
    );
}
