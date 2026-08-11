package com.sdt.feedback.repository.projection;

import java.time.LocalDate;

public interface TrendCountProjection {

    LocalDate getPeriod();

    long getCount();
}
