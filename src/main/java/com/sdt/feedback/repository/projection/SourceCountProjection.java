package com.sdt.feedback.repository.projection;

import com.sdt.feedback.enums.SourceType;

public interface SourceCountProjection {

    SourceType getSource();

    long getCount();
}
