package com.applebay.demo.security;

import com.applebay.demo.exception.RateLimitException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {
    private static final int MAX_ATTEMPTS = 30;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private final Map<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();

    public void check(String key) {
        Deque<Instant> recent = prune(key);
        if (recent.size() >= MAX_ATTEMPTS) {
            throw new RateLimitException("Too many sign-in attempts. Try again later.", WINDOW.toSeconds());
        }
    }

    public void record(String key) {
        prune(key).addLast(Instant.now());
    }

    private Deque<Instant> prune(String key) {
        Instant cutoff = Instant.now().minus(WINDOW);
        Deque<Instant> deque = attempts.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (deque) {
            while (!deque.isEmpty() && deque.peekFirst().isBefore(cutoff)) {
                deque.removeFirst();
            }
            return deque;
        }
    }
}
