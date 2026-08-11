package com.sdt.feedback.repository.projection;

import com.sdt.feedback.enums.FeedbackStatus;

public interface FeedbackStatusCountProjection {

    FeedbackStatus getStatus();

    long getCount();
}
