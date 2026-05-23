package com.SportConnect.demo.dto;

public record UserUpdateRequest(
        String firstName,
        String lastName,
        String phoneNumber
) {
}
