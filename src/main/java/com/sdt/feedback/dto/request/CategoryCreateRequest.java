package com.sdt.feedback.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
        @NotBlank
        @Size(max = 50)
        @Pattern(
                regexp = "^[A-Za-z0-9_]+$",
                message = "code must contain only letters, numbers, and underscores"
        )
        String code,

        @NotBlank
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description
) {
}
