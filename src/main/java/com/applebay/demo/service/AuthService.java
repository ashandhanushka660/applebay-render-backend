package com.applebay.demo.service;

import com.applebay.demo.dto.AuthDtos;
import com.applebay.demo.entity.AppUser;
import com.applebay.demo.exception.BadRequestException;
import com.applebay.demo.exception.RateLimitException;
import com.applebay.demo.repository.AppUserRepository;
import com.applebay.demo.security.ClientIpResolver;
import com.applebay.demo.security.JwtService;
import com.applebay.demo.security.LoginRateLimiter;
import com.applebay.demo.security.PasswordPolicy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginRateLimiter loginRateLimiter;
    private final ClientIpResolver clientIpResolver;
    private final AuditService auditService;
    private final String dummyHash;

    public AuthService(AppUserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       LoginRateLimiter loginRateLimiter,
                       ClientIpResolver clientIpResolver,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.loginRateLimiter = loginRateLimiter;
        this.clientIpResolver = clientIpResolver;
        this.auditService = auditService;
        this.dummyHash = passwordEncoder.encode("not-a-real-password-" + UUID.randomUUID());
    }

    @Transactional(noRollbackFor = {BadCredentialsException.class, RateLimitException.class})
    public AuthDtos.LoginResponse login(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String ip = clientIpResolver.currentIp();
        String limiterKey = "ip:" + ip;
        loginRateLimiter.check(limiterKey);
        loginRateLimiter.record(limiterKey);

        AppUser user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        Instant now = Instant.now();
        if (user == null) {
            passwordEncoder.matches(password, dummyHash);
            auditService.record("AUTH", "LOGIN_FAILED", normalizedEmail, "account", "Unknown account or wrong password");
            throw new BadCredentialsException("Invalid email or password");
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            long seconds = Math.max(1, Duration.between(now, user.getLockedUntil()).toSeconds());
            auditService.record("AUTH", "LOGIN_BLOCKED", normalizedEmail, "account",
                    "Account temporarily locked after failed attempts");
            throw new RateLimitException("Too many failed sign-in attempts. Try again later.", seconds);
        }
        if (!user.isEnabled()) {
            auditService.record("AUTH", "LOGIN_DISABLED", normalizedEmail, "account", "Disabled account attempted to sign in");
            throw new BadCredentialsException("Invalid email or password");
        }
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(normalizedEmail, password));
        } catch (AuthenticationException ex) {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            boolean locked = attempts >= MAX_FAILED_ATTEMPTS;
            if (locked) {
                user.setLockedUntil(now.plus(LOCK_DURATION));
            }
            userRepository.save(user);
            auditService.record("AUTH", locked ? "ACCOUNT_LOCKED" : "LOGIN_FAILED", normalizedEmail, "account",
                    locked ? "Account locked for 15 minutes after repeated failures"
                            : "Failed sign-in attempt " + attempts + " of " + MAX_FAILED_ATTEMPTS);
            throw new BadCredentialsException("Invalid email or password", ex);
        }

        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(now);
        user.setLastLoginIp(ip);
        userRepository.save(user);
        auditService.record("AUTH", "LOGIN_SUCCESS", normalizedEmail, "account", "Successful sign-in");
        return response(user);
    }

    @Transactional(readOnly = true)
    public AuthDtos.UserResponse me(String email) {
        return toUser(requireUser(email));
    }

    @Transactional
    public AuthDtos.LoginResponse changePassword(String email, String currentPassword, String newPassword) {
        AppUser user = requireUser(email);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            auditService.record("AUTH", "PASSWORD_CHANGE_FAILED", email, "account", "Current password did not match");
            throw new BadRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new BadRequestException("Choose a password you have not used here before");
        }
        PasswordPolicy.validate(newPassword);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(Instant.now());
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
        auditService.record("AUTH", "PASSWORD_CHANGED", email, "account", "Password changed; all other sessions revoked");
        return response(user);
    }

    private AuthDtos.LoginResponse response(AppUser user) {
        return new AuthDtos.LoginResponse(
                jwtService.createToken(user.getEmail(), user.getTokenVersion()),
                jwtService.getTtlSeconds(),
                toUser(user)
        );
    }

    private AuthDtos.UserResponse toUser(AppUser user) {
        return new AuthDtos.UserResponse(user.getEmail(), user.getDisplayName(), user.getRole());
    }

    private AppUser requireUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid session"));
    }
}
