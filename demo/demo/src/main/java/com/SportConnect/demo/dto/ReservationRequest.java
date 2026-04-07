package com.SportConnect.demo.dto;

import java.time.LocalDateTime;

public record ReservationRequest(
        Long fieldId,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
