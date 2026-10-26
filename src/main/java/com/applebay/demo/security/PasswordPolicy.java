package com.applebay.demo.security;

import com.applebay.demo.exception.BadRequestException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class PasswordPolicy {
    private static final Set<String> COMMON = Set.of(
            "password", "password1", "password123", "passw0rd", "admin", "admin123",
            "administrator", "welcome", "welcome1", "letmein", "qwerty", "qwerty123",
            "abc123", "iloveyou", "monkey", "dragon", "football", "baseball",
            "sunshine", "princess", "master", "shadow", "superman", "michael",
            "trustno1", "changeme", "secret", "login", "student", "applebay"
    );

    private PasswordPolicy() {
    }

    public static void validate(String password) {
        List<String> problems = new ArrayList<>();
        if (password == null || password.length() < 12) {
            problems.add("Use at least 12 characters.");
        }
        if (password != null && password.getBytes(StandardCharsets.UTF_8).length > 72) {
            problems.add("Use at most 72 bytes (BCrypt limit).");
        }
        if (password == null || !password.chars().anyMatch(Character::isUpperCase)) {
            problems.add("Include an uppercase letter.");
        }
        if (password == null || !password.chars().anyMatch(Character::isLowerCase)) {
            problems.add("Include a lowercase letter.");
        }
        if (password == null || !password.chars().anyMatch(Character::isDigit)) {
            problems.add("Include a number.");
        }
        if (password == null || password.chars().noneMatch(ch -> !Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch))) {
            problems.add("Include a symbol.");
        }
        if (password != null && COMMON.contains(password.toLowerCase(Locale.ROOT))) {
            problems.add("Choose a password that is not commonly used.");
        }
        if (!problems.isEmpty()) {
            throw new BadRequestException(String.join(" ", problems));
        }
    }
}
