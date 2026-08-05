package com.sdt.feedback.dto.request;

import com.sdt.feedback.enums.FeedbackStatus;
import com.sdt.feedback.enums.PriorityLevel;
import com.sdt.feedback.enums.SentimentType;
import com.sdt.feedback.enums.SourceType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class FeedbackFilterRequest {

    @Min(0)
    private Integer page;

    @Min(1)
    @Max(100)
    private Integer size;

    private String sortBy;
    private String sortDirection;
    private SourceType source;
    private FeedbackStatus status;
    private String category;
    private SentimentType sentiment;
    private PriorityLevel priority;
    private String keyword;
    private OffsetDateTime fromDate;
    private OffsetDateTime toDate;
}
