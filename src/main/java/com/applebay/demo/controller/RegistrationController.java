package com.applebay.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.applebay.demo.dto.*;
import com.applebay.demo.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/registrations") @RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RegistrationController {
    private final RegistrationService service;
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public RegistrationResponse register(@Valid @RequestBody RegistrationRequest r) { return service.registerAndEnroll(r); }
}
