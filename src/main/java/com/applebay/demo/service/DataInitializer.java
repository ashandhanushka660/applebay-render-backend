package com.applebay.demo.service;

import com.applebay.demo.entity.AppUser;
import com.applebay.demo.repository.AppUserRepository;
import com.applebay.demo.security.PasswordPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(AppUserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.email}") String adminEmail,
                           @Value("${app.admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        if (adminPassword == null || adminPassword.isBlank()) {
            throw new IllegalStateException("ADMIN_PASSWORD is required for the first startup");
        }
        try {
            PasswordPolicy.validate(adminPassword);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("ADMIN_PASSWORD does not meet the password policy: " + ex.getMessage(), ex);
        }
        AppUser admin = new AppUser();
        admin.setEmail(adminEmail.trim().toLowerCase(Locale.ROOT));
        admin.setDisplayName("System Administrator");
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole("ADMIN");
        admin.setEnabled(true);
        admin.setPasswordChangedAt(Instant.now());
        userRepository.save(admin);
        log.info("Created the initial administrator account for {}", admin.getEmail());
    }
}
