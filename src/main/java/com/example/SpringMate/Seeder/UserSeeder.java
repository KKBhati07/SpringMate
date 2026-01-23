package com.example.SpringMate.Seeder;

import com.example.SpringMate.Shared.Roles;
import com.example.SpringMate.User.Entity.Role;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.User.Repository.RoleRepository;
import com.example.SpringMate.User.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
@Order(2)
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.monitoring.prometheus.username:prometheus}")
    private String prometheusUsername;

    @Value("${app.monitoring.prometheus.password:prometheus-pass}")
    private String prometheusPassword;

    @Override
    public void run(String... args) throws Exception {
        String email = prometheusUsername + "@system.infra";

        Optional<User> existing = userRepository.findByEmail(email);

        if (existing.isPresent()) {
            log.info("Prometheus system user already exists");
            return;
        }

        Role role = roleRepository.findByName(Roles.PROMETHEUS)
                .orElseThrow(() -> new IllegalStateException("PROMETHEUS role not found"));

        System.out.println("[:: THE PASSWORD :: ] "+prometheusPassword);

        userRepository.save(
                User.builder()
                        .email(email)
                        .name(prometheusUsername)
                        .role(role)
                        .password(passwordEncoder.encode(prometheusPassword))
                        .build());

        log.info("Prometheus system user created");

    }
}