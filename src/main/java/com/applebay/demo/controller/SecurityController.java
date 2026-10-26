package com.applebay.demo.controller;

import com.applebay.demo.dto.SecurityDtos;
import com.applebay.demo.repository.AuditEventRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/security")
@PreAuthorize("hasRole('ADMIN')")
public class SecurityController {
    private final AuditEventRepository auditEventRepository;

    public SecurityController(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @GetMapping("/events")
    public List<SecurityDtos.AuditEventResponse> events() {
        return auditEventRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(event -> new SecurityDtos.AuditEventResponse(
                        event.getId(), event.getCategory(), event.getEventType(), event.getActor(),
                        event.getIpAddress(), event.getTarget(), event.getDetail(), event.getCreatedAt()))
                .toList();
    }
}
