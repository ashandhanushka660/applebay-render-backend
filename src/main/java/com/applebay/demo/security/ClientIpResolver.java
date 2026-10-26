package com.applebay.demo.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class ClientIpResolver {
    private final boolean trustProxy;

    public ClientIpResolver(@Value("${app.security.trust-proxy:false}") boolean trustProxy) {
        this.trustProxy = trustProxy;
    }

    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        if (trustProxy) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                String[] hops = forwarded.split(",");
                for (int index = hops.length - 1; index >= 0; index--) {
                    String hop = hops[index].trim();
                    if (!hop.isEmpty()) {
                        return truncate(hop);
                    }
                }
            }
        }
        return truncate(request.getRemoteAddr());
    }

    public String currentIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return resolve(attributes == null ? null : attributes.getRequest());
    }

    private String truncate(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.length() > 64 ? value.substring(0, 64) : value;
    }
}
