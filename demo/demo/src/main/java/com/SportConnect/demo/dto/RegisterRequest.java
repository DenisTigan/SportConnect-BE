package com.SportConnect.demo.dto;

public record RegisterRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        String role
) {
}
