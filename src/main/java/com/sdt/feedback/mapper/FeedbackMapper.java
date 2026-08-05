package com.sdt.feedback.mapper;

import com.sdt.feedback.dto.response.AnalysisResultResponse;
import com.sdt.feedback.dto.response.FeedbackDetailResponse;
import com.sdt.feedback.dto.response.FeedbackListItemResponse;
import com.sdt.feedback.dto.response.RawFeedbackDetailResponse;
import com.sdt.feedback.entity.AnalysisResult;
import com.sdt.feedback.entity.Feedback;
import com.sdt.feedback.entity.RawFeedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface FeedbackMapper {

    @Mapping(target = "id", source = "feedback.id")
    @Mapping(target = "title", source = "feedback.title")
    @Mapping(target = "content", source = "feedback.content")
    @Mapping(target = "authorName", source = "feedback.authorName")
    @Mapping(target = "location", source = "feedback.location")
    @Mapping(target = "category", source = "feedback.category")
    @Mapping(target = "status", source = "feedback.status")
    @Mapping(target = "source", source = "rawFeedback.source")
    @Mapping(target = "receivedAt", source = "rawFeedback.receivedAt")
    @Mapping(target = "sentiment", source = "analysisResult.sentiment")
    @Mapping(target = "sentimentScore", source = "analysisResult.sentimentScore")
    @Mapping(target = "priority", source = "analysisResult.priority")
    @Mapping(target = "priorityScore", source = "analysisResult.priorityScore")
    @Mapping(target = "createdAt", source = "feedback.createdAt")
    FeedbackListItemResponse toListItem(
            Feedback feedback,
            RawFeedback rawFeedback,
            AnalysisResult analysisResult
    );

    RawFeedbackDetailResponse toRawFeedbackDetailResponse(RawFeedback rawFeedback);

    AnalysisResultResponse toAnalysisResultResponse(AnalysisResult analysisResult);

    List<AnalysisResultResponse> toAnalysisResultResponses(
            List<AnalysisResult> analysisResults
    );

    @Mapping(target = "id", source = "feedback.id")
    @Mapping(target = "title", source = "feedback.title")
    @Mapping(target = "content", source = "feedback.content")
    @Mapping(target = "authorName", source = "feedback.authorName")
    @Mapping(target = "authorContact", source = "feedback.authorContact")
    @Mapping(target = "location", source = "feedback.location")
    @Mapping(target = "category", source = "feedback.category")
    @Mapping(target = "status", source = "feedback.status")
    @Mapping(target = "createdAt", source = "feedback.createdAt")
    @Mapping(target = "updatedAt", source = "feedback.updatedAt")
    @Mapping(target = "resolvedAt", source = "feedback.resolvedAt")
    @Mapping(target = "rawFeedback", source = "rawFeedback")
    @Mapping(target = "latestAnalysis", source = "latestAnalysis")
    @Mapping(target = "analysisHistory", source = "analysisHistory")
    FeedbackDetailResponse toDetailResponse(
            Feedback feedback,
            RawFeedbackDetailResponse rawFeedback,
            AnalysisResultResponse latestAnalysis,
            List<AnalysisResultResponse> analysisHistory
    );
}
