package com.applebay.demo.controller;

import com.applebay.demo.dto.AuthDtos;
import com.applebay.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthDtos.LoginResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request.email(), request.password());
    }

    @GetMapping("/me")
    public AuthDtos.UserResponse me(Authentication authentication) {
        return authService.me(authentication.getName());
    }

    @PostMapping("/change-password")
    public AuthDtos.LoginResponse changePassword(Authentication authentication,
                                                 @Valid @RequestBody AuthDtos.ChangePasswordRequest request) {
        return authService.changePassword(authentication.getName(), request.currentPassword(), request.newPassword());
    }
}
