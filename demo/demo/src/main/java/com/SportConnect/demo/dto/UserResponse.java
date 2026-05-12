package com.SportConnect.demo.dto;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String role
) {
}
