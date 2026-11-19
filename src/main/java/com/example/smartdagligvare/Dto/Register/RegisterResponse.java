package com.example.smartdagligvare.Dto.Register;

public record RegisterResponse(
        Long id,
        String username,
        boolean settingsCompleted
) {
}