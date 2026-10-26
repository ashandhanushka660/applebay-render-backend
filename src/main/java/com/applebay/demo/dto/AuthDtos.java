package com.applebay.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Enter a valid email address")
            @Size(max = 150, message = "Email is too long")
            String email,
            @NotBlank(message = "Password is required")
            @Size(max = 200, message = "Password is too long")
            String password
    ) {
    }

    public record UserResponse(String email, String displayName, String role) {
    }

    public record LoginResponse(String token, long expiresIn, UserResponse user) {
    }

    public record ChangePasswordRequest(
            @NotBlank(message = "Current password is required")
            @Size(max = 200, message = "Password is too long")
            String currentPassword,
            @NotBlank(message = "New password is required")
            @Size(min = 12, max = 72, message = "New password must be 12 to 72 characters")
            String newPassword
    ) {
    }
}
