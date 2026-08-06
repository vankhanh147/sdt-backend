package com.sdt.feedback.dto.request;

import jakarta.validation.constraints.Size;

public record CategoryUpdateRequest(
        @Size(max = 100) String name,
        @Size(max = 500) String description
) {
}
