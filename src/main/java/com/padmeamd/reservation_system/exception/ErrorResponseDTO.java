package com.padmeamd.reservation_system.exception;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
        String message,
        String detailedMessage,
        LocalDateTime errorTime
) {}