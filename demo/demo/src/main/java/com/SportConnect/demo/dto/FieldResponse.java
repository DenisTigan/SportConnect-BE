package com.SportConnect.demo.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record FieldResponse(
        Long id,
        String name,
        String address,
        String category,
        Double pricePerHour,
        String phoneNumber,
        String description,
        String imageURL,
        boolean hasLighting,
        boolean isIndoor,
        LocalTime openTime,
        LocalTime closeTime,
        Long ownerId,
        String ownerName
) {
}
