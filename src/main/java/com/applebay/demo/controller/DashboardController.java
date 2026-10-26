package com.applebay.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.applebay.demo.dto.DashboardResponse;
import com.applebay.demo.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/dashboard") @RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {
    private final DashboardService service;
    @GetMapping public DashboardResponse summary() { return service.summary(); }
}
