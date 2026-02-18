package com.amac.ugaras.models.dtos.user;

import java.time.Instant;
import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String email,
        boolean enabled,
        Instant createdAt
) {}