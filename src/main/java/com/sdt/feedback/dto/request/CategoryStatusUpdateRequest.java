package com.sdt.feedback.dto.request;

import jakarta.validation.constraints.NotNull;

public record CategoryStatusUpdateRequest(
        @NotNull Boolean active
) {
}
