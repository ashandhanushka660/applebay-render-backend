package com.applebay.demo.service;

import com.applebay.demo.entity.AuditEvent;
import com.applebay.demo.repository.AuditEventRepository;
import com.applebay.demo.security.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuditService {
    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditEventRepository repository;
    private final ClientIpResolver clientIpResolver;

    public AuditService(AuditEventRepository repository, ClientIpResolver clientIpResolver) {
        this.repository = repository;
        this.clientIpResolver = clientIpResolver;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String category, String eventType, String actor, String target, String detail) {
        try {
            HttpServletRequest request = currentRequest();
            AuditEvent event = new AuditEvent();
            event.setCategory(limit(category, 40));
            event.setEventType(limit(eventType, 60));
            event.setActor(limit(sanitize(actor), 255));
            event.setTarget(limit(sanitize(target), 120));
            event.setDetail(limit(sanitize(detail), 500));
            event.setIpAddress(clientIpResolver.resolve(request));
            if (request != null) {
                event.setUserAgent(limit(sanitize(request.getHeader("User-Agent")), 255));
            }
            repository.save(event);
            log.info("Security event recorded: {}/{}", category, eventType);
        } catch (RuntimeException ex) {
            log.warn("Could not persist security event {}/{}", category, eventType);
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private String sanitize(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("[\\r\\n\\t]", " ").trim();
    }

    private String limit(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() > max ? value.substring(0, max) : value;
    }
}
