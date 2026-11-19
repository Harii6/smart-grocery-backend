package com.example.smartdagligvare.Dto.Register;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Username is required.")
        @Size(
                min = 3,
                max = 50,
                message = "Username must contain 3–50 characters."
        )
        @Pattern(
                regexp = "[a-zA-Z0-9._-]+",
                message = "Username may contain letters, numbers, dots, underscores and hyphens."
        )
        String username,

        @NotBlank(message = "Password is required.")
        @Size(
                min = 15,
                max = 64,
                message = "Password must contain 15–64 characters."
        )
        String password
) {
    @Override
    public String toString() {
        return "RegisterRequest[credentials redacted]";
    }
}