package com.zamorincorp.rideshare.auth.dto;

import java.time.LocalDateTime;

public record UserCreatedEvent(
    String userId,
    String username,
    String role,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
