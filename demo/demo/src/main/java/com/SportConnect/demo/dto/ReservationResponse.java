package com.SportConnect.demo.dto;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long fieldId,
        String fieldName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Double totalPrice,
        String status,
        String clientName
) {
}
