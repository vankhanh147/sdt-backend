package com.sdt.feedback.dto.response;

import com.sdt.feedback.enums.UserRole;

import java.util.UUID;

public record AuthenticatedUserResponse(
        UUID id,
        String username,
        UserRole role
) {
}
