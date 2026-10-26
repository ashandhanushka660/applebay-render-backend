package com.applebay.demo.dto;

import java.time.Instant;

public record SecurityDtos() {
    public record AuditEventResponse(
            Long id,
            String category,
            String eventType,
            String actor,
            String ipAddress,
            String target,
            String detail,
            Instant createdAt
    ) {
    }
}
