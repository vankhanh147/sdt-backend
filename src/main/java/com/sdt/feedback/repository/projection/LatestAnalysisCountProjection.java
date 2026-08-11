package com.sdt.feedback.repository.projection;

public interface LatestAnalysisCountProjection {

    String getSentiment();

    String getPriority();

    long getCount();
}
